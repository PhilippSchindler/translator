package at.ac.tuwien.translator.web.rest;

import at.ac.tuwien.translator.TranslatorApp;

import at.ac.tuwien.translator.domain.Language;
import at.ac.tuwien.translator.domain.Translation;
import at.ac.tuwien.translator.repository.TranslationRepository;

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
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.time.ZoneId;
import java.util.List;

import static at.ac.tuwien.translator.web.rest.TestUtil.sameInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the TranslationResource REST controller.
 *
 * @see TranslationResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TranslatorApp.class)
@Ignore
public class TranslationResourceIntTest {

    private static final String DEFAULT_TEXT = "AAAAAAAAAA";
    private static final String UPDATED_TEXT = "BBBBBBBBBB";

    private static final Boolean DEFAULT_DELETED = false;
    private static final Boolean UPDATED_DELETED = true;

    private static final ZonedDateTime DEFAULT_UPDATED_AT = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_UPDATED_AT = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    @Inject
    private TranslationRepository translationRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Inject
    private EntityManager em;

    private MockMvc restTranslationMockMvc;

    private Translation translation;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        TranslationResource translationResource = new TranslationResource();
        ReflectionTestUtils.setField(translationResource, "translationRepository", translationRepository);
        this.restTranslationMockMvc = MockMvcBuilders.standaloneSetup(translationResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Translation createEntity(EntityManager em) {
        Language language = new Language();
        language.setName("Deutsch");
        language.setShortName("DE");
        em.persist(language);

        Translation translation = new Translation()
                .text(DEFAULT_TEXT)
                .deleted(DEFAULT_DELETED)
                .updatedAt(DEFAULT_UPDATED_AT);
        translation.setLanguage(language);
        return translation;
    }

    @Before
    public void initTest() {
        translation = createEntity(em);
    }

    @Test
    @Transactional
    public void createTranslation() throws Exception {
        int databaseSizeBeforeCreate = translationRepository.findAll().size();

        // Create the Translation

        restTranslationMockMvc.perform(post("/api/translations")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(translation)))
            .andExpect(status().isCreated());

        // Validate the Translation in the database
        List<Translation> translationList = translationRepository.findAll();
        assertThat(translationList).hasSize(databaseSizeBeforeCreate + 1);
        Translation testTranslation = translationList.get(translationList.size() - 1);
        assertThat(testTranslation.getText()).isEqualTo(DEFAULT_TEXT);
        assertThat(testTranslation.isDeleted()).isEqualTo(DEFAULT_DELETED);
        assertThat(testTranslation.getUpdatedAt()).isEqualTo(DEFAULT_UPDATED_AT);
    }

    @Test
    @Transactional
    public void createTranslationWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = translationRepository.findAll().size();

        // Create the Translation with an existing ID
        Translation existingTranslation = new Translation();
        existingTranslation.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restTranslationMockMvc.perform(post("/api/translations")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(existingTranslation)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<Translation> translationList = translationRepository.findAll();
        assertThat(translationList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkTextIsRequired() throws Exception {
        int databaseSizeBeforeTest = translationRepository.findAll().size();
        // set the field null
        translation.setText(null);

        // Create the Translation, which fails.

        restTranslationMockMvc.perform(post("/api/translations")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(translation)))
            .andExpect(status().isBadRequest());

        List<Translation> translationList = translationRepository.findAll();
        assertThat(translationList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkDeletedIsRequired() throws Exception {
        int databaseSizeBeforeTest = translationRepository.findAll().size();
        // set the field null
        translation.setDeleted(null);

        // Create the Translation, which fails.

        restTranslationMockMvc.perform(post("/api/translations")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(translation)))
            .andExpect(status().isBadRequest());

        List<Translation> translationList = translationRepository.findAll();
        assertThat(translationList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkUpdatedAtIsRequired() throws Exception {
        int databaseSizeBeforeTest = translationRepository.findAll().size();
        // set the field null
        translation.setUpdatedAt(null);

        // Create the Translation, which fails.

        restTranslationMockMvc.perform(post("/api/translations")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(translation)))
            .andExpect(status().isBadRequest());

        List<Translation> translationList = translationRepository.findAll();
        assertThat(translationList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllTranslations() throws Exception {
        // Initialize the database
        translationRepository.saveAndFlush(translation);

        // Get all the translationList
        restTranslationMockMvc.perform(get("/api/translations?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(translation.getId().intValue())))
            .andExpect(jsonPath("$.[*].text").value(hasItem(DEFAULT_TEXT.toString())))
            .andExpect(jsonPath("$.[*].deleted").value(hasItem(DEFAULT_DELETED.booleanValue())))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(sameInstant(DEFAULT_UPDATED_AT))));
    }

    @Test
    @Transactional
    public void getTranslation() throws Exception {
        // Initialize the database
        translationRepository.saveAndFlush(translation);

        // Get the translation
        restTranslationMockMvc.perform(get("/api/translations/{id}", translation.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(translation.getId().intValue()))
            .andExpect(jsonPath("$.text").value(DEFAULT_TEXT.toString()))
            .andExpect(jsonPath("$.deleted").value(DEFAULT_DELETED.booleanValue()))
            .andExpect(jsonPath("$.updatedAt").value(sameInstant(DEFAULT_UPDATED_AT)));
    }

    @Test
    @Transactional
    public void getNonExistingTranslation() throws Exception {
        // Get the translation
        restTranslationMockMvc.perform(get("/api/translations/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateTranslation() throws Exception {
        // Initialize the database
        translationRepository.saveAndFlush(translation);
        int databaseSizeBeforeUpdate = translationRepository.findAll().size();

        // Update the translation
        Translation updatedTranslation = translationRepository.findOne(translation.getId());
        updatedTranslation
                .text(UPDATED_TEXT)
                .deleted(UPDATED_DELETED)
                .updatedAt(UPDATED_UPDATED_AT);

        restTranslationMockMvc.perform(put("/api/translations")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedTranslation)))
            .andExpect(status().isOk());

        // Validate the Translation in the database
        List<Translation> translationList = translationRepository.findAll();
        assertThat(translationList).hasSize(databaseSizeBeforeUpdate);
        Translation testTranslation = translationList.get(translationList.size() - 1);
        assertThat(testTranslation.getText()).isEqualTo(UPDATED_TEXT);
        assertThat(testTranslation.isDeleted()).isEqualTo(UPDATED_DELETED);
        assertThat(testTranslation.getUpdatedAt()).isEqualTo(UPDATED_UPDATED_AT);
    }

    @Test
    @Transactional
    public void updateNonExistingTranslation() throws Exception {
        int databaseSizeBeforeUpdate = translationRepository.findAll().size();

        // Create the Translation

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restTranslationMockMvc.perform(put("/api/translations")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(translation)))
            .andExpect(status().isCreated());

        // Validate the Translation in the database
        List<Translation> translationList = translationRepository.findAll();
        assertThat(translationList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteTranslation() throws Exception {
        // Initialize the database
        translationRepository.saveAndFlush(translation);
        int databaseSizeBeforeDelete = translationRepository.findAll().size();

        // Get the translation
        restTranslationMockMvc.perform(delete("/api/translations/{id}", translation.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Translation> translationList = translationRepository.findAll();
        assertThat(translationList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
