package sn.epf.pointage.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cours")
public class Cours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", unique = true, nullable = false, length = 20)
    private String code;

    @Column(name = "intitule", nullable = false, length = 200)
    private String intitule;

    @Column(name = "volume_horaire_total")
    private Integer volumeHoraireTotal;

    @Column(name = "niveau_etude", length = 10)
    private String niveauEtude;

    @Column(name = "filiere", length = 50)
    private String filiere;

    @Column(name = "semestre")
    private Integer semestre;

    @OneToMany(mappedBy = "cours", fetch = FetchType.LAZY)
    private List<Assignation> assignations = new ArrayList<>();

    public Cours() {}

    public Cours(String code, String intitule, String niveauEtude,
                 String filiere, Integer semestre) {
        this.code        = code;
        this.intitule    = intitule;
        this.niveauEtude = niveauEtude;
        this.filiere     = filiere;
        this.semestre    = semestre;
    }

    public Long getId()                          { return id; }
    public String getCode()                      { return code; }
    public void setCode(String c)                { this.code = c; }
    public String getIntitule()                  { return intitule; }
    public void setIntitule(String i)            { this.intitule = i; }
    public Integer getVolumeHoraireTotal()       { return volumeHoraireTotal; }
    public void setVolumeHoraireTotal(Integer v) { this.volumeHoraireTotal = v; }
    public String getNiveauEtude()               { return niveauEtude; }
    public void setNiveauEtude(String n)         { this.niveauEtude = n; }
    public String getFiliere()                   { return filiere; }
    public void setFiliere(String f)             { this.filiere = f; }
    public Integer getSemestre()                 { return semestre; }
    public void setSemestre(Integer s)           { this.semestre = s; }
    public List<Assignation> getAssignations()   { return assignations; }
    @Override
    public String toString() {
        return code + " - " + intitule;
    }
}
