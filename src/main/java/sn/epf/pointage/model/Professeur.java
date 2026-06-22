package sn.epf.pointage.model;

import jakarta.persistence.*;
import sn.epf.pointage.model.enums.TypeContrat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "professeurs")
public class Professeur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "matricule", unique = true, nullable = false, length = 20)
    private String matricule;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "prenom", nullable = false, length = 100)
    private String prenom;

    @Column(name = "email", unique = true, nullable = false, length = 150)
    private String email;

    @Column(name = "telephone", length = 20)
    private String telephone;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_contrat", nullable = false)
    private TypeContrat typeContrat;

    @Column(name = "taux_horaire_xof")
    private Double tauxHoraireXOF;

    @Column(name = "date_embauche")
    private LocalDate dateEmbauche;

    @Column(name = "photo", length = 255)
    private String photo;

    @Column(name = "actif", nullable = false)
    private Boolean actif = true;

    @OneToMany(mappedBy = "professeur", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Assignation> assignations = new ArrayList<>();

    @OneToMany(mappedBy = "professeur", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Pointage> pointages = new ArrayList<>();

    @OneToMany(mappedBy = "professeur", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<RapportMensuel> rapports = new ArrayList<>();

    public Professeur() {}

    public Professeur(String matricule, String nom, String prenom,
                      String email, TypeContrat typeContrat) {
        this.matricule   = matricule;
        this.nom         = nom;
        this.prenom      = prenom;
        this.email       = email;
        this.typeContrat = typeContrat;
        this.actif       = true;
    }

    public Long getId()                        { return id; }
    public String getMatricule()               { return matricule; }
    public void setMatricule(String m)         { this.matricule = m; }
    public String getNom()                     { return nom; }
    public void setNom(String n)               { this.nom = n; }
    public String getPrenom()                  { return prenom; }
    public void setPrenom(String p)            { this.prenom = p; }
    public String getEmail()                   { return email; }
    public void setEmail(String e)             { this.email = e; }
    public String getTelephone()               { return telephone; }
    public void setTelephone(String t)         { this.telephone = t; }
    public TypeContrat getTypeContrat()        { return typeContrat; }
    public void setTypeContrat(TypeContrat tc) { this.typeContrat = tc; }
    public Double getTauxHoraireXOF()          { return tauxHoraireXOF; }
    public void setTauxHoraireXOF(Double t)   { this.tauxHoraireXOF = t; }
    public LocalDate getDateEmbauche()         { return dateEmbauche; }
    public void setDateEmbauche(LocalDate d)   { this.dateEmbauche = d; }
    public String getPhoto()                   { return photo; }
    public void setPhoto(String p)             { this.photo = p; }
    public Boolean getActif()                  { return actif; }
    public void setActif(Boolean a)            { this.actif = a; }
    public List<Assignation> getAssignations() { return assignations; }
    public List<Pointage> getPointages()       { return pointages; }
    public List<RapportMensuel> getRapports()  { return rapports; }

    @Override
    public String toString() {
        return nom + " " + prenom;
    }
}
