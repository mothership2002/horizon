package horizon.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConductorRegistryTest {

    private ConductorRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new ConductorRegistry();
    }

    @Test
    void shouldRegisterAndFindExactMatch() {
        // Given
        TestConductor conductor = new TestConductor("user.create");
        registry.register(conductor);

        // When
        Conductor<String, String> found = registry.find("user.create");

        // Then
        assertThat(found).isEqualTo(conductor);
    }

    @Test
    void shouldRegisterAndFindPatternMatch() {
        // Given
        TestConductor conductor = new TestConductor("user.*");
        registry.register(conductor);

        // When
        Conductor<String, String> found1 = registry.find("user.create");
        Conductor<String, String> found2 = registry.find("user.update");
        Conductor<String, String> notFound = registry.find("order.create");

        // Then
        assertThat(found1).isEqualTo(conductor);
        assertThat(found2).isEqualTo(conductor);
        assertThat(notFound).isNull();
    }

    private static class TestConductor implements Conductor<String, String> {
        private final String pattern;

        TestConductor(String pattern) {
            this.pattern = pattern;
        }

        @Override
        public String conduct(String payload) {
            return "Conducted: " + payload;
        }

        @Override
        public String getIntentPattern() {
            return pattern;
        }
    }
}
