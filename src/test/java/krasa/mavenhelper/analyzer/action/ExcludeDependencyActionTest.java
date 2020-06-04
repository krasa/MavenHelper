package krasa.mavenhelper.analyzer.action;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ExcludeDependencyActionTest {

    class Foo {
        String get() { return "original"; }
    }

    @Test
    public void dummy() {
        Foo dummy = mock(Foo.class);
        when(dummy.get()).thenReturn("mocked");
        assertThat(dummy.get()).isEqualTo("mocked");
    }

}