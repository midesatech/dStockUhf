package infrastructure.adapter.serial;

import help.Architecture;

import java.util.Optional;

public class SerialFactory {
    public Optional<Serial> getSerial(Architecture architecture) {
        if (Architecture.AMD64.equals(architecture) || Architecture.AARCH64.equals(architecture)) {
            return Optional.of(SerialAmdImpl.getInstance());
        }
        return Optional.empty();
    }
}
