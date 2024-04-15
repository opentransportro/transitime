package org.transitclock.utils;

import org.junit.jupiter.api.Test;
import org.transitclock.domain.structs.Location;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class GeoTest {

    @Test
    void offset() {
        Location l = new Location(37.79971, -122.43595);
        Location offset = Geo.offset(l, 20.0, -70.0);
        assertThat(offset.getLat()).isEqualTo(37.79908047487586);
        assertThat(offset.getLon()).isEqualTo(-122.43572236920325);
    }

    @Test
    void format() {
        assertThat(Geo.format(-122.43572236920325)).isEqualTo("-122.43572");
    }

    @Test
    void rightOffsetVertex() {
        double d = 30.0;
        Location l1 = new Location(37.800, -122.436);
        Location l2 = new Location(37.805, -122.436);
        Location l3 = new Location(37.801, -122.437);
        Location offsetVertex = Geo.rightOffsetVertex(l1, l2, l3, d);

        assertThat(offsetVertex.getLat()).isEqualTo(37.804597227613876);
        assertThat(offsetVertex.getLon()).isEqualTo(-122.43604986481728);
        //
        //		Location loc = new Location(40.75, -73.97);
        //		Location l1 = new Location(40.7486, -73.9864);
        //		Location l2 = new Location(40.7586, -73.9664);
        //		Vector v1 = new Vector(l1, l2);
        //		Vector v2 = new Vector(l2, l1);
        //		double vlength = v1.length();
        //
        //		double d1 = distance(loc,  v1);
        //		double md1 = matchDistanceAlongVector(loc, v1);
        //
        //		double d2 = distance(loc,  v2);
        //		double md2 = matchDistanceAlongVector(loc, v2);
        //
        //		// Make sure the values are ok. Should get the same distance
        //		// no matter which was vector is pointing.
        //		assert Math.abs(d1-d2) < 0.000001;
        //		// The distance to the match for the vectors going in
        //		// different directions should add up to v1length
        //		assert Math.abs(md1 + md2-vlength) < 0.000001;
        //
        //		System.out.println("vlength=" + vlength + "\n" +
        //				"d1=" + dFormat(d1) + " md1=" + dFormat(md1) + "\n" +
        //				"d2=" + dFormat(d2) + " md2=" + dFormat(md2) + "\n");
    }
}