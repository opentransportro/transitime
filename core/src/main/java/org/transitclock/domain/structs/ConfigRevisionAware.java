package org.transitclock.domain.structs;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@Getter
@RequiredArgsConstructor
@MappedSuperclass
public abstract class ConfigRevisionAware {
    @Column(name = "config_rev")
    @Id
    private final int configRev;

    public ConfigRevisionAware() {
        configRev = -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigRevisionAware that = (ConfigRevisionAware) o;
        return configRev == that.configRev;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(configRev);
    }
}
