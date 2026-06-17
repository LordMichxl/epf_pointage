package sn.epf.pointage.model;

import jakarta.persistence.*;
import sn.epf.pointage.model.enums.Role;

@Entity
@Table(name = "utilisateurs")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login", unique = true, nullable = false, length = 100)
    private String login;

    @Column(name = "mot_de_passe_hash", nullable = false, length = 60)
    private String motDePasseHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professeur_id", nullable = true)
    private Professeur professeurLie;

    public Utilisateur() {}

    public Utilisateur(String login, String motDePasseHash, Role role) {
        this.login          = login;
        this.motDePasseHash = motDePasseHash;
        this.role           = role;
    }

    public Long getId()                        { return id; }
    public String getLogin()                   { return login; }
    public void setLogin(String l)             { this.login = l; }
    public String getMotDePasseHash()          { return motDePasseHash; }
    public void setMotDePasseHash(String m)    { this.motDePasseHash = m; }
    public Role getRole()                      { return role; }
    public void setRole(Role r)                { this.role = r; }
    public Professeur getProfesseurLie()       { return professeurLie; }
    public void setProfesseurLie(Professeur p) { this.professeurLie = p; }
}
