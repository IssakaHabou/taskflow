package ci.inphb.taskflow;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "taskflow.demo-data=false"
})
@AutoConfigureMockMvc
class TaskflowApplicationTests {

    @Autowired
    private MockMvc mvc;

    @Test
    void apiCrudFonctionne() throws Exception {
        mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Test PaaS\",\"priority\":\"HAUTE\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("A_FAIRE"));

        mvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test PaaS"));

        mvc.perform(get("/api/tasks/stats"))
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void titreObligatoire() throws Exception {
        mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").exists());
    }

    @Test
    void pagesWebAccessibles() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk());
        mvc.perform(get("/tasks/new")).andExpect(status().isOk());
        mvc.perform(get("/plateforme")).andExpect(status().isOk());
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }
}
