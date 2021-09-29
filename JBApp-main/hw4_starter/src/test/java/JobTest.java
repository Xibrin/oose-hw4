import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import model.Employer;
import model.Job;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.*;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JobTest {

private final String URI = "jdbc:sqlite:./JBApp.db";

@Nested
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JobORMLiteDaoTest {

    private ConnectionSource connectionSource;
    private Dao<Job, Integer> dao;

    @BeforeAll
    public void setUpAll() throws SQLException {
        connectionSource = new JdbcConnectionSource(URI);
        TableUtils.createTableIfNotExists(connectionSource, Job.class);
        dao = DaoManager.createDao(connectionSource, Job.class);
    }

    // delete all rows in the jobs table before each test case
    @BeforeEach
    public void setUpEach() throws SQLException {
        TableUtils.clearTable(connectionSource, Job.class);
    }

    @Test
        public void testCreateNullFields() {
            Job j = new Job(null,null, null, null, null, true, true, null, 0, null);
            Assertions.assertThrows(SQLException.class, () -> dao.create(j));
        }

        @Test
        public void testCreateMultipleJobs() throws SQLException {
            Employer e = new Employer("Salesforce", "Tech", "An American cloud-based software company focused on customer relationship management services!");
            List<Job> jobs = new ArrayList<>();
            jobs.add(new Job("SWE",new Date(2021, 6, 2), new Date(2021, 12, 1), "tech", "NYC", true, true, "Must be familiar with Java", 100000, e));
            jobs.add(new Job("SDE ",new Date(2021, 6, 5), new Date(2021, 12, 1), "tech", "Chicago", true, true, "Must be familiar with Java", 120000, e));
            jobs.add(new Job("Programmer",new Date(2021, 6, 1), new Date(2021, 12, 1), "tech", "LA", true, true, "Must be familiar with Java", 108000, e));
            jobs.add(new Job("Software Engineer",new Date(2021, 6, 6), new Date(2021, 12, 1), "tech", "SF", true, true, "Must be familiar with Java", 102000, e));
            jobs.add(new Job("SWE PM",new Date(2021, 7, 1), new Date(2021, 12, 1), "tech", "Seattle", true, true, "Must be familiar with Java", 110000, e));

            dao.create(jobs);

            List<Job> jobRead = dao.queryForAll();

            assertEquals(jobRead, jobs);
        }

        @Test
        public void testUpdatingJobPay() throws SQLException {
            Employer e = new Employer("First Solar", "Energy", "A leading global provider of comprehensive PV solar solutions!");
            Job newJ = new Job("SWE",new Date(2021, 6, 2), new Date(2021, 12, 1), "tech", "NYC", true, true, "Must be familiar with Java", 100000, e);
            newJ.setId(1234);
            dao.create(newJ);
            newJ.setPayAmount(130000);
            dao.createOrUpdate(newJ);
            Job inserted = dao.queryForId(1234);
            assertEquals(130000, inserted.getPayAmount());
        }
        @Test
        public void testNonUniqueJobTitle() {
            Employer e = new Employer("First Solar", "Energy", "A leading global provider of comprehensive PV solar solutions!");
            List<Job> jobs = new ArrayList<>();

            Job j1 = new Job("SWE",new Date(2021, 7, 2), new Date(2021, 9, 1), "tech", "LA", true, true, "Must be familiar with Java", 120000, e);
            Job j2 = new Job("SDE",new Date(2021, 6, 2), new Date(2021, 12, 1), "tech", "NYC", true, true, "Must be familiar with Java", 100000, e);

            jobs.add(j1);
            jobs.add(j2);

            Assertions.assertThrows(SQLException.class, () -> dao.create(jobs));
        }

        @Test
        public void testDeleteJob() throws SQLException {
            Employer e = new Employer("First Solar", "Energy", "A leading global provider of comprehensive PV solar solutions!");

            Job j1 = new Job("SWE",new Date(2021, 7, 2), new Date(2021, 9, 1), "tech", "LA", true, true, "Must be familiar with Java", 120000, e);
            dao.create(j1);
            List<Job> jobs = dao.queryForEq("title", j1.getTitle());
            assertEquals("SWE", jobs.get(0).getTitle());
            dao.delete(j1);
            List<Job> removed = dao.queryForEq("title", j1.getTitle());
            assertEquals(removed.size(), 0);
        }

        @Test
        public void testRemoveSomeJobs() throws SQLException {
            Employer e = new Employer("First Solar", "Energy", "A leading global provider of comprehensive PV solar solutions!");
            List<Job> jobs = new ArrayList<>();

            Job j1 = new Job("SWE",new Date(2021, 7, 2), new Date(2021, 9, 1), "tech", "LA", true, true, "Must be familiar with Java", 120000, e);
            Job j2 = new Job("SDE",new Date(2021, 6, 2), new Date(2021, 12, 1), "tech", "NYC", true, true, "Must be familiar with Java", 100000, e);
            Job j3 = new Job("SDE I",new Date(2021, 6, 4), new Date(2021, 12, 1), "tech", "SF", true, true, "Must be familiar with Java", 100000, e);

            jobs.add(j1);
            jobs.add(j2);
            jobs.add(j3);

            dao.create(jobs);
            dao.delete(j2);

            List<Job> removed = dao.queryForEq("title", j2.getTitle());
            assertEquals(removed.size(), 0);
        }

        @Test
        public void testUpdateIDExisting() throws SQLException{
            Employer e = new Employer("First Solar", "Energy", "A leading global provider of comprehensive PV solar solutions!");

            Job j1 = new Job("SWE",new Date(2021, 7, 2), new Date(2021, 9, 1), "tech", "LA", true, true, "Must be familiar with Java", 120000, e);
            Job j2 = new Job("SDE",new Date(2021, 6, 2), new Date(2021, 12, 1), "tech", "NYC", true, true, "Must be familiar with Java", 100000, e);
            Job j3 = new Job("SDE I",new Date(2021, 6, 4), new Date(2021, 12, 1), "tech", "SF", true, true, "Must be familiar with Java", 100000, e);

            dao.create(j1);
            dao.create(j2);
            dao.create(j3);

            List<Job> jobs = dao.queryForAll();
            Assertions.assertThrows(SQLException.class, () -> dao.updateId(j1, jobs.get(1).getId()));
        }

        @Test
        public void testCreateJobSameInfoDifferentTitle() throws SQLException {
            Employer e = new Employer("First Solar", "Energy", "A leading global provider of comprehensive PV solar solutions!");
            Job j1 = new Job("SWE",new Date(2021, 7, 2), new Date(2021, 9, 1), "tech", "LA", true, true, "Must be familiar with Java", 120000, e);
            Job j2 = new Job("SDE",new Date(2021, 7, 2), new Date(2021, 9, 1), "tech", "LA", true, true, "Must be familiar with Java", 120000, e);

            dao.create(j1);
            Assertions.assertDoesNotThrow(() -> dao.create(j2));
        }

        @Test
        public void testUpdateMultipleTimes() throws SQLException {
            Employer e = new Employer("First Solar", "Energy", "A leading global provider of comprehensive PV solar solutions!");
            Job j1 = new Job("SWE",new Date(2021, 7, 2), new Date(2021, 9, 1), "tech", "LA", true, true, "Must be familiar with Java", 120000, e);
            dao.create(j1);
            j1.setDomain("Finance");
            j1.setDomain("Academia");
            j1.setDomain("Business");

            assertEquals("Business", dao.queryForAll().get(0).getDomain());
        }

        @Test
        public void testUpdateID() throws SQLException {
            Employer e = new Employer("First Solar", "Energy", "A leading global provider of comprehensive PV solar solutions!");
            Job j1 = new Job("SWE",new Date(2021, 7, 2), new Date(2021, 9, 1), "tech", "LA", true, true, "Must be familiar with Java", 120000, e);
            dao.create(j1);
            assertEquals(j1.id, 1);
        }

        @Test
        public void testDeleteAllItems() throws SQLException {
            Employer e = new Employer("Salesforce", "Tech", "An American cloud-based software company focused on customer relationship management services!");
            List<Job> jobs = new ArrayList<>();
            jobs.add(new Job("SWE",new Date(2021, 6, 2), new Date(2021, 12, 1), "tech", "NYC", true, true, "Must be familiar with Java", 100000, e));
            jobs.add(new Job("SDE ",new Date(2021, 6, 5), new Date(2021, 12, 1), "tech", "Chicago", true, true, "Must be familiar with Java", 120000, e));
            jobs.add(new Job("Programmer",new Date(2021, 6, 1), new Date(2021, 12, 1), "tech", "LA", true, true, "Must be familiar with Java", 108000, e));
            jobs.add(new Job("Software Engineer",new Date(2021, 6, 6), new Date(2021, 12, 1), "tech", "SF", true, true, "Must be familiar with Java", 102000, e));
            jobs.add(new Job("SWE PM",new Date(2021, 7, 1), new Date(2021, 12, 1), "tech", "Seattle", true, true, "Must be familiar with Java", 110000, e));

            dao.create(jobs);

            doa.delete(jobs);

            assertEquals(0, dao.queryForAll().size());
        }

        @Test
        public void testDelete() throws SQLException {
        }

    // TODO 5: Similar to what was done in EmployerTest.EmployerORMLiteDaoTest class, write JUnit tests
    //  to test basic CRUD operations on the jobs table! Think of interesting test cases and
    //  write at least four different test cases for each of the C(reate)/U(pdate)/D(elete)
    //  operations!
    //  Note: You need to (write code to) create the "jobs" table before writing your test cases!
}


@Nested
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JobAPITest {

    final String BASE_URL = "http://localhost:7000";
    private OkHttpClient client;

    @BeforeAll
    public void setUpAll() {
        client = new OkHttpClient();
    }

    @Test
    public void testHTTPGetJobsEndPoint() throws IOException {
        // TODO 6: Write code to send a http get request using OkHttp to the
        //  "jobs" endpoint and assert that the received status code is OK (200)!
        //  Note: In order for this to work, you need to make sure your local sparkjava
        //  server is running, before you run the JUnit test!
    }
}
}
