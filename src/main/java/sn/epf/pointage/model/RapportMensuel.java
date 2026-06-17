package sn.epf.pointage.model;

import jakarta.persistence.*;
import sn.epf.pointage.model.enums.StatutRapport;

@Entity
@Table(name = "rapports_mensuels")
public class RapportMensuel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mois", nullable = false)
    private Integer mois;

    @Column(name = "annee", nullable = false)
    private Integer annee;

    @Column(name = "heures_realisees")
    private Double heuresRealisees;

    @Column(name = "montant_xof")
    private Long montantXOF;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutRapport statut = StatutRapport.EN_ATTENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professeur_id", nullable = false)
    private Professeur professeur;

    public RapportMensuel() {}

    public RapportMensuel(Professeur professeur, Integer mois, Integer annee) {
        this.professeur = professeur;
        this.mois       = mois;
        this.annee      = annee;
        this.statut     = StatutRapport.EN_ATTENTE;
    }

    public Long getId()                      { return id; }
    public Integer getMois()                 { return mois; }
    public void setMois(Integer m)           { this.mois = m; }
    public Integer getAnnee()                { return annee; }
    public void setAnnee(Integer a)          { this.annee = a; }
    public Double getHeuresRealisees()       { return heuresRealisees; }
    public void setHeuresRealisees(Double h) { this.heuresRealisees = h; }
    public Long getMontantXOF()              { return montantXOF; }
    public void setMontantXOF(Long m)        { this.montantXOF = m; }
    public StatutRapport getStatut()         { return statut; }
    public void setStatut(StatutRapport s)   { this.statut = s; }
    public Professeur getProfesseur()        { return professeur; }
    public void setProfesseur(Professeur p)  { this.professeur = p; }
}
