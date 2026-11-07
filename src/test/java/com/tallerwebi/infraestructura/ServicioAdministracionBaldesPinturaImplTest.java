package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import com.tallerwebi.dominio.BaldePintura;
import com.tallerwebi.dominio.RepositorioLitros;
import com.tallerwebi.dominio.ServicioAdministracionBaldesPintura;
import com.tallerwebi.dominio.ServicioAdministracionBaldesPinturaImpl;
import com.tallerwebi.dominio.ServicioScrapingPinturas;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.SessionFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HibernateInfraestructuraTestConfig.class })
public class ServicioAdministracionBaldesPinturaImplTest {

  private static final String HTML_DOS_BALDES =
    "" +
    "<html><body>" +
    "<div class='vtex-product-summary-2-x-container'>" +
    "  <a href='/pintura-uno/p'></a>" +
    "  <h3>Pintura Uno</h3>" +
    "  <span class='vtex-product-price-1-x-sellingPrice'>$ 9.000,00</span>" +
    "  <div class='vtex-store-components-3-x-skuSelectorSubcontainer--presentacion'>" +
    "    <div class='vtex-store-components-3-x-skuSelectorItem'>1 Lt.</div>" +
    "    <div class='vtex-store-components-3-x-skuSelectorItem'>4 Lts.</div>" +
    "    <div class='vtex-store-components-3-x-skuSelectorItem'>10 Lts.</div>" +
    "  </div>" +
    "</div>" +
    "<div class='vtex-product-summary-2-x-container'>" +
    "  <a href='/pintura-dos/p'></a>" +
    "  <h3>Pintura Dos</h3>" +
    "  <span class='vtex-product-price-1-x-sellingPrice'>$ 10.000,00</span>" +
    "  <div class='vtex-store-components-3-x-skuSelectorSubcontainer--presentacion'>" +
    "    <div class='vtex-store-components-3-x-skuSelectorItem'>1 Lt.</div>" +
    "    <div class='vtex-store-components-3-x-skuSelectorItem'>4 Lts.</div>" +
    "    <div class='vtex-store-components-3-x-skuSelectorItem'>10 Lts.</div>" +
    "  </div>" +
    "</div>" +
    "</body></html>";

  @Autowired
  private SessionFactory sessionFactory;

  @Test
  @Transactional
  public void deberiaReutilizarLitrosYPersistirLasOpcionesDeTodosLosBaldes() throws Exception {
    ParserBaldesColorshop parser = new ParserBaldesColorshop();
    Document doc = Jsoup.parse(HTML_DOS_BALDES);
    List<BaldePintura> baldes = new ArrayList<>();
    for (BaldeScrapeado scrapeado : parser.parsear(doc)) {
      baldes.add(scrapeado.aBaldePintura());
    }

    ServicioScrapingPinturas scraping = () -> baldes;
    RepositorioBaldePinturaImpl repositorioBaldePintura = new RepositorioBaldePinturaImpl(
      this.sessionFactory
    );
    RepositorioLitros repositorioLitros = new RepositorioLitrosImpl(this.sessionFactory);
    ServicioAdministracionBaldesPintura servicio = new ServicioAdministracionBaldesPinturaImpl(
      scraping,
      repositorioBaldePintura,
      repositorioLitros
    );

    servicio.actualizarBaldesDesdeColorshop();

    this.sessionFactory.getCurrentSession().flush();
    this.sessionFactory.getCurrentSession().clear();

    List<BaldePintura> guardados =
      this.sessionFactory.getCurrentSession()
        .createQuery("FROM BaldePintura", BaldePintura.class)
        .getResultList();
    assertThat(guardados, hasSize(2));
    for (BaldePintura balde : guardados) {
      assertThat(balde.getOpcionesLitros(), is("1,4,10"));
    }

    Long cantidadLitros =
      this.sessionFactory.getCurrentSession()
        .createQuery("SELECT COUNT(l) FROM Litros l", Long.class)
        .getSingleResult();
    assertThat(cantidadLitros, is(3L));

    Long cantidadBaldeLitros =
      this.sessionFactory.getCurrentSession()
        .createQuery("SELECT COUNT(bl) FROM BaldeLitros bl", Long.class)
        .getSingleResult();
    assertThat(cantidadBaldeLitros, is(6L));
  }
}
