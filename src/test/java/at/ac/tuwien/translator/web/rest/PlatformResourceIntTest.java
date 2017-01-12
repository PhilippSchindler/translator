package at.ac.tuwien.translator.web.rest;

import at.ac.tuwien.translator.TranslatorApp;

import at.ac.tuwien.translator.domain.Platform;
import at.ac.tuwien.translator.repository.PlatformRepository;

import org.junit.Before;
import org.junit.Ignore;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the PlatformResource REST controller.
 *
 * @see PlatformResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TranslatorApp.class)
@Ignore
public class PlatformResourceIntTest {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    @Inject
    private PlatformRepository platformRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Inject
    private EntityManager em;

    private MockMvc restPlatformMockMvc;

    private Platform platform;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        PlatformResource platformResource = new PlatformResource();
        ReflectionTestUtils.setField(platformResource, "platformRepository", platformRepository);
        this.restPlatformMockMvc = MockMvcBuilders.standaloneSetup(platformResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Platform createEntity(EntityManager em) {
        Platform platform = new Platform()
                .name(DEFAULT_NAME);
        return platform;
    }

    @Before
    public void initTest() {
        platform = createEntity(em);
    }

    @Test
    @Transactional
    public void createPlatform() throws Exception {
        int databaseSizeBeforeCreate = platformRepository.findAll().size();

        // Create the Platform

        restPlatformMockMvc.perform(post("/api/platforms")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(platform)))
            .andExpect(status().isCreated());

        // Validate the Platform in the database
        List<Platform> platformList = platformRepository.findAll();
        assertThat(platformList).hasSize(databaseSizeBeforeCreate + 1);
        Platform testPlatform = platformList.get(platformList.size() - 1);
        assertThat(testPlatform.getName()).isEqualTo(DEFAULT_NAME);
    }

    @Test
    @Transactional
    public void createPlatformWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = platformRepository.findAll().size();

        // Create the Platform with an existing ID
        Platform existingPlatform = new Platform();
        existingPlatform.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restPlatformMockMvc.perform(post("/api/platforms")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(existingPlatform)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<Platform> platformList = platformRepository.findAll();
        assertThat(platformList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = platformRepository.findAll().size();
        // set the field null
        platform.setName(null);

        // Create the Platform, which fails.

        restPlatformMockMvc.perform(post("/api/platforms")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(platform)))
            .andExpect(status().isBadRequest());

        List<Platform> platformList = platformRepository.findAll();
        assertThat(platformList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllPlatforms() throws Exception {
        // Initialize the database
        platformRepository.saveAndFlush(platform);

        // Get all the platformList
        restPlatformMockMvc.perform(get("/api/platforms?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(platform.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())));
    }

    @Test
    @Transactional
    public void getPlatform() throws Exception {
        // Initialize the database
        platformRepository.saveAndFlush(platform);

        // Get the platform
        restPlatformMockMvc.perform(get("/api/platforms/{id}", platform.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(platform.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingPlatform() throws Exception {
        // Get the platform
        restPlatformMockMvc.perform(get("/api/platforms/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updatePlatform() throws Exception {
        // Initialize the database
        platformRepository.saveAndFlush(platform);
        int databaseSizeBeforeUpdate = platformRepository.findAll().size();

        // Update the platform
        Platform updatedPlatform = platformRepository.findOne(platform.getId());
        updatedPlatform
                .name(UPDATED_NAME);

        restPlatformMockMvc.perform(put("/api/platforms")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedPlatform)))
            .andExpect(status().isOk());

        // Validate the Platform in the database
        List<Platform> platformList = platformRepository.findAll();
        assertThat(platformList).hasSize(databaseSizeBeforeUpdate);
        Platform testPlatform = platformList.get(platformList.size() - 1);
        assertThat(testPlatform.getName()).isEqualTo(UPDATED_NAME);
    }

    @Test
    @Transactional
    public void updateNonExistingPlatform() throws Exception {
        int databaseSizeBeforeUpdate = platformRepository.findAll().size();

        // Create the Platform

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restPlatformMockMvc.perform(put("/api/platforms")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(platform)))
            .andExpect(status().isCreated());

        // Validate the Platform in the database
        List<Platform> platformList = platformRepository.findAll();
        assertThat(platformList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deletePlatform() throws Exception {
        // Initialize the database
        platformRepository.saveAndFlush(platform);
        int databaseSizeBeforeDelete = platformRepository.findAll().size();

        // Get the platform
        restPlatformMockMvc.perform(delete("/api/platforms/{id}", platform.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Platform> platformList = platformRepository.findAll();
        assertThat(platformList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
