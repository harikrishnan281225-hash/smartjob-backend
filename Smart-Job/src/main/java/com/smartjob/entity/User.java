package com.smartjob.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true)
    private String username;

    @JsonIgnore
    @Column(nullable = true)
    private String password;

    private String role;
    private String phone;
    private String provider;

    // --- PORTFOLIO & PROFILE FEATURES ---

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String portfolioUrl;

    private String linkedInUrl;
    private String githubUrl;
    private String profilePhotoPath;

    private String resumePath;

    private boolean jobAlertEnabled = false;

    // Fixed: Excluded from toString to prevent StackOverflow, ignored in JSON
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @ToString.Exclude
    private List<Application> applications;

    // --- UserDetails Implementation ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String formattedRole = (role != null && role.startsWith("ROLE_")) ? role : "ROLE_" + (role != null ? role.toUpperCase() : "USER");
        return List.of(new SimpleGrantedAuthority(formattedRole));
    }

    @Override
    public String getUsername() { return username != null ? username : email; }

    @Override
    public String getPassword() { return password; }

    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }
}