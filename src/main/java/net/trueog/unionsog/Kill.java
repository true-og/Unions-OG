package net.trueog.unionsog;

import java.time.LocalDateTime;

public class Kill {

    private final UnionPlayer killer;
    private final UnionPlayer victim;
    private final LocalDateTime time;

    public Kill(UnionPlayer killer, UnionPlayer victim, LocalDateTime time) {

        this.killer = killer;
        this.victim = victim;
        this.time = time;

    }

    public UnionPlayer getKiller() {

        return killer;

    }

    public UnionPlayer getVictim() {

        return victim;

    }

    public LocalDateTime getTime() {

        return time;

    }

    public enum Type {

        CIVILIAN("c"), RIVAL("r"), NEUTRAL("n"), ALLY("a");

        Type(String shortName) {

            this.shortName = shortName;

        }

        private final String shortName;

        public String getShortname() {

            return shortName;

        }

    }

}
