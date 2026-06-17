package sn.epf.pointage.model;

import jakarta.persistence.*;
import sn.epf.pointage.model.enums.Frequence;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "periodicites_cours")
public class PeriodiciteCours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "jour_semaine", nullable = false)
    private DayOfWeek jourSemaine;

    @Column(name = "heure_debut", nullable = false)
    private LocalTime heureDebut;

    @Column(name = "heure_fin", nullable = false)
    private LocalTime heureFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequence", nullable = false)
    private Frequence frequence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignation_id", nullable = false)
    private Assignation assignation;

    public PeriodiciteCours() {}

    public PeriodiciteCours(Assignation assignation, DayOfWeek jourSemaine,
                             LocalTime heureDebut, LocalTime heureFin, Frequence frequence) {
        this.assignation = assignation;
        this.jourSemaine = jourSemaine;
        this.heureDebut  = heureDebut;
        this.heureFin    = heureFin;
        this.frequence   = frequence;
    }

    public Long getId()                      { return id; }
    public DayOfWeek getJourSemaine()        { return jourSemaine; }
    public void setJourSemaine(DayOfWeek j)  { this.jourSemaine = j; }
    public LocalTime getHeureDebut()         { return heureDebut; }
    public void setHeureDebut(LocalTime h)   { this.heureDebut = h; }
    public LocalTime getHeureFin()           { return heureFin; }
    public void setHeureFin(LocalTime h)     { this.heureFin = h; }
    public Frequence getFrequence()          { return frequence; }
    public void setFrequence(Frequence f)    { this.frequence = f; }
    public Assignation getAssignation()      { return assignation; }
    public void setAssignation(Assignation a){ this.assignation = a; }
}
