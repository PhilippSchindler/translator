package at.ac.tuwien.translator.web.rest;

import at.ac.tuwien.translator.TranslatorApp;

import at.ac.tuwien.translator.domain.LogEntry;
import at.ac.tuwien.translator.repository.LogEntryRepository;

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
 * Test class for the LogEntryResource REST controller.
 *
 * @see LogEntryResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TranslatorApp.class)
public class LogEntryResourceIntTest {

    private static final ZonedDateTime DEFAULT_TIMESTAMP = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_TIMESTAMP = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final String DEFAULT_MESSAGE = "AAAAAAAAAA";
    private static final String UPDATED_MESSAGE = "BBBBBBBBBB";

    private static final String DEFAULT_RESULT = "AAAAAAAAAA";
    private static final String UPDATED_RESULT = "BBBBBBBBBB";

    @Inject
    private LogEntryRepository logEntryRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Inject
    private EntityManager em;

    private MockMvc restLogEntryMockMvc;

    private LogEntry logEntry;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        LogEntryResource logEntryResource = new LogEntryResource();
        ReflectionTestUtils.setField(logEntryResource, "logEntryRepository", logEntryRepository);
        this.restLogEntryMockMvc = MockMvcBuilders.standaloneSetup(logEntryResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static LogEntry createEntity(EntityManager em) {
        LogEntry logEntry = new LogEntry()
                .timestamp(DEFAULT_TIMESTAMP)
                .message(DEFAULT_MESSAGE)
                .result(DEFAULT_RESULT);
        return logEntry;
    }

    @Before
    public void initTest() {
        logEntry = createEntity(em);
    }

    @Test
    @Transactional
    public void createLogEntry() throws Exception {
        int databaseSizeBeforeCreate = logEntryRepository.findAll().size();

        // Create the LogEntry

        restLogEntryMockMvc.perform(post("/api/log-entries")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(logEntry)))
            .andExpect(status().isCreated());

        // Validate the LogEntry in the database
        List<LogEntry> logEntryList = logEntryRepository.findAll();
        assertThat(logEntryList).hasSize(databaseSizeBeforeCreate + 1);
        LogEntry testLogEntry = logEntryList.get(logEntryList.size() - 1);
        assertThat(testLogEntry.getTimestamp()).isEqualTo(DEFAULT_TIMESTAMP);
        assertThat(testLogEntry.getMessage()).isEqualTo(DEFAULT_MESSAGE);
        assertThat(testLogEntry.getResult()).isEqualTo(DEFAULT_RESULT);
    }

    @Test
    @Transactional
    public void createLogEntryWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = logEntryRepository.findAll().size();

        // Create the LogEntry with an existing ID
        LogEntry existingLogEntry = new LogEntry();
        existingLogEntry.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restLogEntryMockMvc.perform(post("/api/log-entries")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(existingLogEntry)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<LogEntry> logEntryList = logEntryRepository.findAll();
        assertThat(logEntryList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkTimestampIsRequired() throws Exception {
        int databaseSizeBeforeTest = logEntryRepository.findAll().size();
        // set the field null
        logEntry.setTimestamp(null);

        // Create the LogEntry, which fails.

        restLogEntryMockMvc.perform(post("/api/log-entries")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(logEntry)))
            .andExpect(status().isBadRequest());

        List<LogEntry> logEntryList = logEntryRepository.findAll();
        assertThat(logEntryList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkMessageIsRequired() throws Exception {
        int databaseSizeBeforeTest = logEntryRepository.findAll().size();
        // set the field null
        logEntry.setMessage(null);

        // Create the LogEntry, which fails.

        restLogEntryMockMvc.perform(post("/api/log-entries")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(logEntry)))
            .andExpect(status().isBadRequest());

        List<LogEntry> logEntryList = logEntryRepository.findAll();
        assertThat(logEntryList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkResultIsRequired() throws Exception {
        int databaseSizeBeforeTest = logEntryRepository.findAll().size();
        // set the field null
        logEntry.setResult(null);

        // Create the LogEntry, which fails.

        restLogEntryMockMvc.perform(post("/api/log-entries")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(logEntry)))
            .andExpect(status().isBadRequest());

        List<LogEntry> logEntryList = logEntryRepository.findAll();
        assertThat(logEntryList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllLogEntries() throws Exception {
        // Initialize the database
        logEntryRepository.saveAndFlush(logEntry);

        // Get all the logEntryList
        restLogEntryMockMvc.perform(get("/api/log-entries?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(logEntry.getId().intValue())))
            .andExpect(jsonPath("$.[*].timestamp").value(hasItem(sameInstant(DEFAULT_TIMESTAMP))))
            .andExpect(jsonPath("$.[*].message").value(hasItem(DEFAULT_MESSAGE.toString())))
            .andExpect(jsonPath("$.[*].result").value(hasItem(DEFAULT_RESULT.toString())));
    }

    @Test
    @Transactional
    public void getLogEntry() throws Exception {
        // Initialize the database
        logEntryRepository.saveAndFlush(logEntry);

        // Get the logEntry
        restLogEntryMockMvc.perform(get("/api/log-entries/{id}", logEntry.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(logEntry.getId().intValue()))
            .andExpect(jsonPath("$.timestamp").value(sameInstant(DEFAULT_TIMESTAMP)))
            .andExpect(jsonPath("$.message").value(DEFAULT_MESSAGE.toString()))
            .andExpect(jsonPath("$.result").value(DEFAULT_RESULT.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingLogEntry() throws Exception {
        // Get the logEntry
        restLogEntryMockMvc.perform(get("/api/log-entries/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateLogEntry() throws Exception {
        // Initialize the database
        logEntryRepository.saveAndFlush(logEntry);
        int databaseSizeBeforeUpdate = logEntryRepository.findAll().size();

        // Update the logEntry
        LogEntry updatedLogEntry = logEntryRepository.findOne(logEntry.getId());
        updatedLogEntry
                .timestamp(UPDATED_TIMESTAMP)
                .message(UPDATED_MESSAGE)
                .result(UPDATED_RESULT);

        restLogEntryMockMvc.perform(put("/api/log-entries")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedLogEntry)))
            .andExpect(status().isOk());

        // Validate the LogEntry in the database
        List<LogEntry> logEntryList = logEntryRepository.findAll();
        assertThat(logEntryList).hasSize(databaseSizeBeforeUpdate);
        LogEntry testLogEntry = logEntryList.get(logEntryList.size() - 1);
        assertThat(testLogEntry.getTimestamp()).isEqualTo(UPDATED_TIMESTAMP);
        assertThat(testLogEntry.getMessage()).isEqualTo(UPDATED_MESSAGE);
        assertThat(testLogEntry.getResult()).isEqualTo(UPDATED_RESULT);
    }

    @Test
    @Transactional
    public void updateNonExistingLogEntry() throws Exception {
        int databaseSizeBeforeUpdate = logEntryRepository.findAll().size();

        // Create the LogEntry

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restLogEntryMockMvc.perform(put("/api/log-entries")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(logEntry)))
            .andExpect(status().isCreated());

        // Validate the LogEntry in the database
        List<LogEntry> logEntryList = logEntryRepository.findAll();
        assertThat(logEntryList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteLogEntry() throws Exception {
        // Initialize the database
        logEntryRepository.saveAndFlush(logEntry);
        int databaseSizeBeforeDelete = logEntryRepository.findAll().size();

        // Get the logEntry
        restLogEntryMockMvc.perform(delete("/api/log-entries/{id}", logEntry.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<LogEntry> logEntryList = logEntryRepository.findAll();
        assertThat(logEntryList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
