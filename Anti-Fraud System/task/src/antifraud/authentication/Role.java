package antifraud.authentication;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum Role {
    ADMINISTRATOR("ROLE_ADMINISTRATOR"),
    SUPPORT("ROLE_SUPPORT"),
    MERCHANT("ROLE_MERCHANT");

    final String asStringWithRolePrefix;

    Role(String asStringWithRolePrefix) {
        this.asStringWithRolePrefix = asStringWithRolePrefix;
    }

    public static List<String> valuesAsStrings() {
        return Arrays.stream(Role.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }
}
