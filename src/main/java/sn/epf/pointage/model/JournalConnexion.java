package sn.epf.pointage.model;

import jakarta.persistence.*;
import sn.epf.pointage.model.enums.TypeAction;
import java.time.LocalDateTime;

@Entity
@Table(name = "journal_connexions")
public class JournalConnexion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Column(name = "horodatage", nullable = false)
    private LocalDateTime horodatage;

    @Column(name = "adresse_ip", length = 45)
    private String adresseIp;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private TypeAction action;

    public JournalConnexion() {}

    public JournalConnexion(Utilisateur utilisateur, String adresseIp, TypeAction action) {
        this.utilisateur = utilisateur;
        this.adresseIp = adresseIp;
        this.action = action;
        this.horodatage = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Utilisateur getUtilisateur() { return utilisateur; }
    public LocalDateTime getHorodatage() { return horodatage; }
    public String getAdresseIp() { return adresseIp; }
    public TypeAction getAction() { return action; }
}