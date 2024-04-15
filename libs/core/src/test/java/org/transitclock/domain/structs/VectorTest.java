package org.transitclock.domain.structs;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class VectorTest {

    @Test
    void angle() {
        Vector v = new Vector(new Location(37.79971, -122.43595), new Location(37.79972, -122.43596));
        assertThat(v.angle()).isEqualTo(2.2395072418082322);
    }

    @Test
    void heading() {
        Vector v = new Vector(new Location(37.79971, -122.43595), new Location(37.79972, -122.43596));
        assertThat(v.heading()).isEqualTo(-38.3143131445956);
    }
}