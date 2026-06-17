package sn.epf.pointage.model;

import jakarta.persistence.*;
import sn.epf.pointage.model.enums.StatutSeance;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "seances_planifiees")
public class SeancePlanifiee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_heure", nullable = false)
    private LocalDateTime dateHeure;

    @Column(name = "duree_minutes")
    private Integer dureeMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutSeance statut = StatutSeance.PLANIFIEE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignation_id", nullable = false)
    private Assignation assignation;

    @OneToMany(mappedBy = "seance", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Pointage> pointages = new ArrayList<>();

    public SeancePlanifiee() {}

    public SeancePlanifiee(Assignation assignation, LocalDateTime dateHeure, Integer dureeMinutes) {
        this.assignation  = assignation;
        this.dateHeure    = dateHeure;
        this.dureeMinutes = dureeMinutes;
        this.statut       = StatutSeance.PLANIFIEE;
    }

    public Long getId()                       { return id; }
    public LocalDateTime getDateHeure()       { return dateHeure; }
    public void setDateHeure(LocalDateTime d) { this.dateHeure = d; }
    public Integer getDureeMinutes()          { return dureeMinutes; }
    public void setDureeMinutes(Integer d)    { this.dureeMinutes = d; }
    public StatutSeance getStatut()           { return statut; }
    public void setStatut(StatutSeance s)     { this.statut = s; }
    public Assignation getAssignation()       { return assignation; }
    public void setAssignation(Assignation a) { this.assignation = a; }
    public List<Pointage> getPointages()      { return pointages; }
}
