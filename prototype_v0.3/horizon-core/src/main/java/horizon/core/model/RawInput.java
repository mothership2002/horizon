package horizon.core.model;

public interface RawInput extends Raw {

    String getSource();

    String getScheme();

    byte[] getBody();
}
