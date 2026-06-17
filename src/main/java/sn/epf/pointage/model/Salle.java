package sn.epf.pointage.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "salles")
public class Salle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom", nullable = false, length = 50)
    private String nom;

    @Column(name = "capacite")
    private Integer capacite;

    @Column(name = "batiment", length = 100)
    private String batiment;

    @Column(name = "equipements", length = 255)
    private String equipements;

    @OneToMany(mappedBy = "salle", fetch = FetchType.LAZY)
    private List<Assignation> assignations = new ArrayList<>();

    public Salle() {}

    public Salle(String nom, Integer capacite, String batiment) {
        this.nom      = nom;
        this.capacite = capacite;
        this.batiment = batiment;
    }

    public Long getId()                        { return id; }
    public String getNom()                     { return nom; }
    public void setNom(String n)               { this.nom = n; }
    public Integer getCapacite()               { return capacite; }
    public void setCapacite(Integer c)         { this.capacite = c; }
    public String getBatiment()                { return batiment; }
    public void setBatiment(String b)          { this.batiment = b; }
    public String getEquipements()             { return equipements; }
    public void setEquipements(String e)       { this.equipements = e; }
    public List<Assignation> getAssignations() { return assignations; }
}
