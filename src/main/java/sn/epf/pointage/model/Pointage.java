package sn.epf.pointage.model;

import jakarta.persistence.*;
import sn.epf.pointage.model.enums.StatutPointage;
import sn.epf.pointage.model.enums.TypePointage;
import java.time.LocalDateTime;

@Entity
@Table(name = "pointages")
public class Pointage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "heure_pointage", nullable = false)
    private LocalDateTime heurePointage;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_pointage", nullable = false)
    private TypePointage typePointage;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutPointage statut;

    @Column(name = "observations", length = 500)
    private String observations;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seance_id", nullable = false)
    private SeancePlanifiee seance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professeur_id", nullable = false)
    private Professeur professeur;

    public Pointage() {}

    public Pointage(SeancePlanifiee seance, Professeur professeur,
                    LocalDateTime heurePointage, TypePointage typePointage,
                    StatutPointage statut) {
        this.seance        = seance;
        this.professeur    = professeur;
        this.heurePointage = heurePointage;
        this.typePointage  = typePointage;
        this.statut        = statut;
    }

    public Long getId()                          { return id; }
    public LocalDateTime getHeurePointage()      { return heurePointage; }
    public void setHeurePointage(LocalDateTime h){ this.heurePointage = h; }
    public TypePointage getTypePointage()        { return typePointage; }
    public void setTypePointage(TypePointage t)  { this.typePointage = t; }
    public StatutPointage getStatut()            { return statut; }
    public void setStatut(StatutPointage s)      { this.statut = s; }
    public String getObservations()              { return observations; }
    public void setObservations(String o)        { this.observations = o; }
    public SeancePlanifiee getSeance()           { return seance; }
    public void setSeance(SeancePlanifiee s)     { this.seance = s; }
    public Professeur getProfesseur()            { return professeur; }
    public void setProfesseur(Professeur p)      { this.professeur = p; }
}
