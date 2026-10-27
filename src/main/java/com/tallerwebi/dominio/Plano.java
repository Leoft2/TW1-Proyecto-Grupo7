package com.tallerwebi.dominio;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Entity
public class Plano {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(cascade = CascadeType.ALL)
  private Ambiente ambiente;

  @ManyToOne
  @JoinColumn(name = "usuario_id")
  private Usuario usuario;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "plano_id")
  @OrderColumn(name = "posicion")
  private List<MuebleUbicado> muebles;

  @ManyToMany
  @JoinTable(name = "plano_muebles_excluidos")
  private List<Mueble> mueblesExcluidos;

  @ElementCollection
  @CollectionTable(name = "plano_motivos_exclusion", joinColumns = @JoinColumn(name = "plano_id"))
  @Column(name = "motivo")
  private List<String> motivosDeExclusion;

  private String mensaje;

  private LocalDateTime fechaDeCreacion;

  private Boolean activa;

  public Plano() {}

  public Plano(
    Ambiente ambiente,
    List<MuebleUbicado> muebles,
    List<Mueble> mueblesExcluidos,
    List<String> motivosDeExclusion
  ) {
    this.ambiente = ambiente;
    this.muebles = muebles;
    this.mueblesExcluidos = mueblesExcluidos;
    this.motivosDeExclusion = motivosDeExclusion;
  }

  public List<MuebleUbicado> getMuebles() {
    return muebles;
  }

  public List<MuebleUbicado> getMueblesEnConflicto() {
    return muebles.stream().filter(MuebleUbicado::isEnConflicto).toList();
  }

  public String descripcionDelConflicto(MuebleUbicado ubicado) {
    String obstaculos = ambiente
      .getObstaculos()
      .stream()
      .filter(obstaculo -> obstaculo.colisionaCon(ubicado, ambiente))
      .map(Obstaculo::getDescripcion)
      .collect(Collectors.joining(", "));
    return ubicado.getMueble().getNombre() + " choca con " + obstaculos;
  }

  public List<Mueble> getMueblesExcluidos() {
    return mueblesExcluidos;
  }

  public List<String> getMotivosDeExclusion() {
    return motivosDeExclusion;
  }

  public Ambiente getAmbiente() {
    return ambiente;
  }

  public Long getId() {
    return id;
  }

  public Usuario getUsuario() {
    return usuario;
  }

  public void setUsuario(Usuario usuario) {
    this.usuario = usuario;
  }

  public String getMensaje() {
    return mensaje;
  }

  public void setMensaje(String mensaje) {
    this.mensaje = mensaje;
  }

  public LocalDateTime getFechaDeCreacion() {
    return fechaDeCreacion;
  }

  public void setFechaDeCreacion(LocalDateTime fechaDeCreacion) {
    this.fechaDeCreacion = fechaDeCreacion;
  }

  public boolean isActiva() {
    return Boolean.TRUE.equals(activa);
  }

  public void setActiva(boolean activa) {
    this.activa = activa;
  }
}
