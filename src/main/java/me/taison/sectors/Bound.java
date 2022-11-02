package me.taison.sectors;

import org.bukkit.Location;

import java.util.Optional;

import static me.taison.sectors.Sectors.*;
import static me.taison.sectors.Sectors.boundN;

public enum Bound {
    N,
    E,
    W,
    S;

    public static Optional<Bound> getBound(Location location) {

        if (location.getBlockX() >= boundE) {
            return Optional.of(Bound.E);

        } else if (location.getBlockX() <= boundW) {
            return Optional.of(Bound.W);

        } else if (location.getBlockZ() >= boundS) {
            return Optional.of(Bound.S);

        } else if (location.getBlockZ() <= boundN) {
            return Optional.of(Bound.N);
        }

        return Optional.empty();
    }

    public static boolean isOutOfBound(Location location) {

        if (location.getBlockX() > boundE) {
            return true;

        } if (location.getBlockX() < boundW) {
            return true;

        } if (location.getBlockZ() > boundS) {
            return true;

        } if (location.getBlockZ() < boundN) {
            return true;
        }

        return false;
    }
}
