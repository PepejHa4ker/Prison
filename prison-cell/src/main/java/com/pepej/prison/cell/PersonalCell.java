package com.pepej.prison.cell;

import com.pepej.prison.cell.transform.crusher.Crusher;
import com.pepej.prison.cell.transform.extractor.Extractor;
import com.pepej.prison.cell.transform.furnace.Furnace;

import java.util.Set;
import java.util.UUID;

public class PersonalCell {


    private final UUID owner;
    private final Crusher crusher;
    private final Furnace furnance;
    private final Extractor exctractor;
    private final Set<Worker> workers;

    public PersonalCell(
            UUID owner,
            Crusher crusher,
            Furnace furnance,
            Extractor exctractor,
            Set<Worker> workers
    ) {
        this.owner = owner;
        this.crusher = crusher;
        this.furnance = furnance;
        this.exctractor = exctractor;
        this.workers = workers;
    }

    public UUID getOwner() {
        return owner;
    }

    public Crusher getCrusher() {
        return crusher;
    }

    public Furnace getFurnance() {
        return furnance;
    }

    public Extractor getExctractor() {
        return exctractor;
    }

    public Set<Worker> getWorkers() {
        return workers;
    }
}
