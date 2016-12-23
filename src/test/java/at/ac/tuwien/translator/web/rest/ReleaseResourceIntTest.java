package at.ac.tuwien.translator.web.rest;

import at.ac.tuwien.translator.TranslatorApp;

import at.ac.tuwien.translator.domain.Release;
import at.ac.tuwien.translator.repository.ReleaseRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the ReleaseResource REST controller.
 *
 * @see ReleaseResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TranslatorApp.class)
public class ReleaseResourceIntTest {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_DEADLINE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_DEADLINE = LocalDate.now(ZoneId.systemDefault());

    @Inject
    private ReleaseRepository releaseRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Inject
    private EntityManager em;

    private MockMvc restReleaseMockMvc;

    private Release release;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ReleaseResource releaseResource = new ReleaseResource();
        ReflectionTestUtils.setField(releaseResource, "releaseRepository", releaseRepository);
        this.restReleaseMockMvc = MockMvcBuilders.standaloneSetup(releaseResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Release createEntity(EntityManager em) {
        Release release = new Release()
                .name(DEFAULT_NAME)
                .deadline(DEFAULT_DEADLINE);
        return release;
    }

    @Before
    public void initTest() {
        release = createEntity(em);
    }

    @Test
    @Transactional
    public void createRelease() throws Exception {
        int databaseSizeBeforeCreate = releaseRepository.findAll().size();

        // Create the Release

        restReleaseMockMvc.perform(post("/api/releases")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(release)))
            .andExpect(status().isCreated());

        // Validate the Release in the database
        List<Release> releaseList = releaseRepository.findAll();
        assertThat(releaseList).hasSize(databaseSizeBeforeCreate + 1);
        Release testRelease = releaseList.get(releaseList.size() - 1);
        assertThat(testRelease.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testRelease.getDeadline()).isEqualTo(DEFAULT_DEADLINE);
    }

    @Test
    @Transactional
    public void createReleaseWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = releaseRepository.findAll().size();

        // Create the Release with an existing ID
        Release existingRelease = new Release();
        existingRelease.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restReleaseMockMvc.perform(post("/api/releases")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(existingRelease)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<Release> releaseList = releaseRepository.findAll();
        assertThat(releaseList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = releaseRepository.findAll().size();
        // set the field null
        release.setName(null);

        // Create the Release, which fails.

        restReleaseMockMvc.perform(post("/api/releases")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(release)))
            .andExpect(status().isBadRequest());

        List<Release> releaseList = releaseRepository.findAll();
        assertThat(releaseList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkDeadlineIsRequired() throws Exception {
        int databaseSizeBeforeTest = releaseRepository.findAll().size();
        // set the field null
        release.setDeadline(null);

        // Create the Release, which fails.

        restReleaseMockMvc.perform(post("/api/releases")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(release)))
            .andExpect(status().isBadRequest());

        List<Release> releaseList = releaseRepository.findAll();
        assertThat(releaseList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllReleases() throws Exception {
        // Initialize the database
        releaseRepository.saveAndFlush(release);

        // Get all the releaseList
        restReleaseMockMvc.perform(get("/api/releases?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(release.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].deadline").value(hasItem(DEFAULT_DEADLINE.toString())));
    }

    @Test
    @Transactional
    public void getRelease() throws Exception {
        // Initialize the database
        releaseRepository.saveAndFlush(release);

        // Get the release
        restReleaseMockMvc.perform(get("/api/releases/{id}", release.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(release.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.deadline").value(DEFAULT_DEADLINE.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingRelease() throws Exception {
        // Get the release
        restReleaseMockMvc.perform(get("/api/releases/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateRelease() throws Exception {
        // Initialize the database
        releaseRepository.saveAndFlush(release);
        int databaseSizeBeforeUpdate = releaseRepository.findAll().size();

        // Update the release
        Release updatedRelease = releaseRepository.findOne(release.getId());
        updatedRelease
                .name(UPDATED_NAME)
                .deadline(UPDATED_DEADLINE);

        restReleaseMockMvc.perform(put("/api/releases")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedRelease)))
            .andExpect(status().isOk());

        // Validate the Release in the database
        List<Release> releaseList = releaseRepository.findAll();
        assertThat(releaseList).hasSize(databaseSizeBeforeUpdate);
        Release testRelease = releaseList.get(releaseList.size() - 1);
        assertThat(testRelease.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testRelease.getDeadline()).isEqualTo(UPDATED_DEADLINE);
    }

    @Test
    @Transactional
    public void updateNonExistingRelease() throws Exception {
        int databaseSizeBeforeUpdate = releaseRepository.findAll().size();

        // Create the Release

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restReleaseMockMvc.perform(put("/api/releases")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(release)))
            .andExpect(status().isCreated());

        // Validate the Release in the database
        List<Release> releaseList = releaseRepository.findAll();
        assertThat(releaseList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteRelease() throws Exception {
        // Initialize the database
        releaseRepository.saveAndFlush(release);
        int databaseSizeBeforeDelete = releaseRepository.findAll().size();

        // Get the release
        restReleaseMockMvc.perform(delete("/api/releases/{id}", release.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Release> releaseList = releaseRepository.findAll();
        assertThat(releaseList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
