package za.co.absa.subatomic;

import org.junit.Test;
import org.junit.runner.RunWith;
import za.co.absa.subatomic.infrastructure.NucleusConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GluonApplicationTest {

    @Autowired
    private NucleusConfiguration nucleusConfiguration;

    @Test
    public void spring_context_loads_successfully() {
        assertThat(nucleusConfiguration).isNotNull();
    }
}
