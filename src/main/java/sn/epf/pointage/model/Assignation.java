package sn.epf.pointage.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assignations")
public class Assignation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "annee_academique", nullable = false, length = 10)
    private String anneeAcademique;

    @Column(name = "heures_prevues")
    private Integer heuresPrevues;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professeur_id", nullable = false)
    private Professeur professeur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cours_id", nullable = false)
    private Cours cours;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salle_id")
    private Salle salle;

    @OneToMany(mappedBy = "assignation", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<PeriodiciteCours> periodicites = new ArrayList<>();

    @OneToMany(mappedBy = "assignation", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<SeancePlanifiee> seances = new ArrayList<>();

    public Assignation() {}

    public Assignation(Professeur professeur, Cours cours, Salle salle,
                       String anneeAcademique, Integer heuresPrevues) {
        this.professeur      = professeur;
        this.cours           = cours;
        this.salle           = salle;
        this.anneeAcademique = anneeAcademique;
        this.heuresPrevues   = heuresPrevues;
    }

    public Long getId()                             { return id; }
    public String getAnneeAcademique()              { return anneeAcademique; }
    public void setAnneeAcademique(String a)        { this.anneeAcademique = a; }
    public Integer getHeuresPrevues()               { return heuresPrevues; }
    public void setHeuresPrevues(Integer h)         { this.heuresPrevues = h; }
    public Professeur getProfesseur()               { return professeur; }
    public void setProfesseur(Professeur p)         { this.professeur = p; }
    public Cours getCours()                         { return cours; }
    public void setCours(Cours c)                   { this.cours = c; }
    public Salle getSalle()                         { return salle; }
    public void setSalle(Salle s)                   { this.salle = s; }
    public List<PeriodiciteCours> getPeriodicites() { return periodicites; }
    public List<SeancePlanifiee> getSeances()       { return seances; }
}
