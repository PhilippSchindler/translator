package at.ac.tuwien.translator.web.rest;

import at.ac.tuwien.translator.TranslatorApp;
import at.ac.tuwien.translator.domain.Definition;
import at.ac.tuwien.translator.repository.DefinitionRepository;
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
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the DefinitionResource REST controller.
 *
 * @see DefinitionResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TranslatorApp.class)
public class DefinitionResourceIntTest {

    private static final String DEFAULT_LABEL = "AAAAAAAAAA";
    private static final String UPDATED_LABEL = "BBBBBBBBBB";

    private static final String DEFAULT_TEXT = "AAAAAAAAAA";
    private static final String UPDATED_TEXT = "BBBBBBBBBB";

    private static final Integer DEFAULT_VERSION = 0;
    private static final Integer UPDATED_VERSION = 1;

    @Inject
    private DefinitionRepository definitionRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Inject
    private EntityManager em;

    private MockMvc restDefinitionMockMvc;

    private Definition definition;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        DefinitionResource definitionResource = new DefinitionResource();
        ReflectionTestUtils.setField(definitionResource, "definitionRepository", definitionRepository);
        this.restDefinitionMockMvc = MockMvcBuilders.standaloneSetup(definitionResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Definition createEntity(EntityManager em) {
        return new Definition()
                .label(DEFAULT_LABEL)
                .text(DEFAULT_TEXT)
            .version(DEFAULT_VERSION)
            .createdAt(ZonedDateTime.now())
            .updatedAt(ZonedDateTime.now());
    }

    @Before
    public void initTest() {
        definition = createEntity(em);
    }

    @Test
    @Transactional
    public void createDefinition() throws Exception {
        int databaseSizeBeforeCreate = definitionRepository.findAll().size();

        // Create the Definition

        restDefinitionMockMvc.perform(post("/api/definitions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(definition)))
            .andExpect(status().isCreated());

        // Validate the Definition in the database
        List<Definition> definitionList = definitionRepository.findAll();
        assertThat(definitionList).hasSize(databaseSizeBeforeCreate + 1);
        Definition testDefinition = definitionList.get(definitionList.size() - 1);
        assertThat(testDefinition.getLabel()).isEqualTo(DEFAULT_LABEL);
        assertThat(testDefinition.getText()).isEqualTo(DEFAULT_TEXT);
        assertThat(testDefinition.getVersion()).isEqualTo(DEFAULT_VERSION);
        assertThat(testDefinition.getCreatedAt()).isBefore(ZonedDateTime.now());
        assertThat(testDefinition.getUpdatedAt()).isBefore(ZonedDateTime.now());
    }

    @Test
    @Transactional
    public void createDefinitionWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = definitionRepository.findAll().size();

        // Create the Definition with an existing ID
        Definition existingDefinition = new Definition();
        existingDefinition.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restDefinitionMockMvc.perform(post("/api/definitions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(existingDefinition)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<Definition> definitionList = definitionRepository.findAll();
        assertThat(definitionList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkLabelIsRequired() throws Exception {
        int databaseSizeBeforeTest = definitionRepository.findAll().size();
        // set the field null
        definition.setLabel(null);

        // Create the Definition, which fails.

        restDefinitionMockMvc.perform(post("/api/definitions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(definition)))
            .andExpect(status().isBadRequest());

        List<Definition> definitionList = definitionRepository.findAll();
        assertThat(definitionList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTextIsRequired() throws Exception {
        int databaseSizeBeforeTest = definitionRepository.findAll().size();
        // set the field null
        definition.setText(null);

        // Create the Definition, which fails.

        restDefinitionMockMvc.perform(post("/api/definitions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(definition)))
            .andExpect(status().isBadRequest());

        List<Definition> definitionList = definitionRepository.findAll();
        assertThat(definitionList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllDefinitions() throws Exception {
        // Initialize the database
        definitionRepository.saveAndFlush(definition);

        // Get all the definitionList
        restDefinitionMockMvc.perform(get("/api/definitions?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(definition.getId().intValue())))
            .andExpect(jsonPath("$.[*].label").value(hasItem(DEFAULT_LABEL.toString())))
            .andExpect(jsonPath("$.[*].text").value(hasItem(DEFAULT_TEXT.toString())))
            .andExpect(jsonPath("$.[*].version").value(hasItem(DEFAULT_VERSION)));
    }

    @Test
    @Transactional
    public void getDefinition() throws Exception {
        // Initialize the database
        definitionRepository.saveAndFlush(definition);

        // Get the definition
        restDefinitionMockMvc.perform(get("/api/definitions/{id}", definition.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(definition.getId().intValue()))
            .andExpect(jsonPath("$.label").value(DEFAULT_LABEL.toString()))
            .andExpect(jsonPath("$.text").value(DEFAULT_TEXT.toString()))
            .andExpect(jsonPath("$.version").value(DEFAULT_VERSION));
    }

    @Test
    @Transactional
    public void getNonExistingDefinition() throws Exception {
        // Get the definition
        restDefinitionMockMvc.perform(get("/api/definitions/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateDefinition() throws Exception {
        // Initialize the database
        definitionRepository.saveAndFlush(definition);
        int databaseSizeBeforeUpdate = definitionRepository.findAll().size();

        // Update the definition
        Definition updatedDefinition = definitionRepository.findOne(definition.getId());
        updatedDefinition
                .label(UPDATED_LABEL)
                .text(UPDATED_TEXT)
                .version(UPDATED_VERSION);

        restDefinitionMockMvc.perform(put("/api/definitions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedDefinition)))
            .andExpect(status().isCreated());

        // Validate the Definition in the database
        List<Definition> definitionList = definitionRepository.findAll();
        assertThat(definitionList).hasSize(databaseSizeBeforeUpdate);
        Definition testDefinition = definitionList.get(definitionList.size() - 1);
        assertThat(testDefinition.getLabel()).isEqualTo(UPDATED_LABEL);
        assertThat(testDefinition.getText()).isEqualTo(UPDATED_TEXT);
        assertThat(testDefinition.getVersion()).isEqualTo(UPDATED_VERSION);
    }

    @Test
    @Transactional
    public void updateNonExistingDefinition() throws Exception {
        int databaseSizeBeforeUpdate = definitionRepository.findAll().size();

        // Create the Definition

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restDefinitionMockMvc.perform(put("/api/definitions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(definition)))
            .andExpect(status().isCreated());

        // Validate the Definition in the database
        List<Definition> definitionList = definitionRepository.findAll();
        assertThat(definitionList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteDefinition() throws Exception {
        // Initialize the database
        definitionRepository.saveAndFlush(definition);
        int databaseSizeBeforeDelete = definitionRepository.findAll().size();

        // Get the definition
        restDefinitionMockMvc.perform(delete("/api/definitions/{id}", definition.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Definition> definitionList = definitionRepository.findAll();
        assertThat(definitionList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
