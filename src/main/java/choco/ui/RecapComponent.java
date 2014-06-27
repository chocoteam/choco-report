package choco.ui;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class RecapComponent extends CustomComponent {

    // Récupération des valeurs qu'on a besoin pour faire le récapitulatif
    public RecapComponent(String benchmark1, String benchmark2, int nbTotalBugsB1,
                          int nbTotalBugsB2, int nbBugs1, int nbBugs2, int nbBugs3,
                          int nbHausse, int nbPerte) {

        // Layout principal
        final VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSizeFull();

        // Layout du récapitulatif des bugs
        final VerticalLayout bugs = new VerticalLayout();
        bugs.setMargin(true);
        bugs.setSizeFull();

        // Création de chaque label pour le récapitulatif des bugs
        Label nomBugs = new Label();
        nomBugs.setContentMode(Label.CONTENT_XHTML);
        nomBugs.setValue("<b> Bugs : ");
        bugs.addComponent(nomBugs);
        bugs.addComponent(new Label("&nbsp;", Label.CONTENT_XHTML));

        Label nombresTotalBugsB1 = new Label();
        nombresTotalBugsB1
                .setValue("Nombre total de bugs trouvés pour le benchmark 1 : "
                        + nbTotalBugsB1);
        nombresTotalBugsB1.setImmediate(true);
        bugs.addComponent(nombresTotalBugsB1);
        bugs.addComponent(new Label("&nbsp;", Label.CONTENT_XHTML));

        Label nombresTotalBugsB2 = new Label();
        nombresTotalBugsB2
                .setValue("Nombre total de bugs trouvés pour le benchmark 2 : "
                        + nbTotalBugsB2);
        nombresTotalBugsB1.setImmediate(true);
        bugs.addComponent(nombresTotalBugsB2);
        bugs.addComponent(new Label("&nbsp;", Label.CONTENT_XHTML));

        Label nombreBugs1 = new Label();
        nombreBugs1
                .setValue("Nombre de bugs trouvés pour le bug \"Temps de résolution < 900000 et pas de solution\" : "
                        + nbBugs1);
        nombreBugs1.setImmediate(true);
        bugs.addComponent(nombreBugs1);
        bugs.addComponent(new Label("&nbsp;", Label.CONTENT_XHTML));

        Label nombreBugs2 = new Label();
        nombreBugs2
                .setValue("Nombre de bugs trouvés pour le bug \"Valeurs exceptionnelles\" : "
                        + nbBugs2);
        nombreBugs2.setImmediate(true);
        bugs.addComponent(nombreBugs2);
        bugs.addComponent(new Label("&nbsp;", Label.CONTENT_XHTML));

        Label nombreBugs3 = new Label();
        nombreBugs3
                .setValue("Nombre de bugs trouvés pour le bug \"Temps de résolution < 900000 et pas la meilleure solution\" : "
                        + nbBugs3);
        nombreBugs3.setImmediate(true);
        bugs.addComponent(nombreBugs3);
        bugs.addComponent(new Label("&nbsp;", Label.CONTENT_XHTML));

        layout.addComponent(bugs);

        // Layout du récapitulatif des performances
        final VerticalLayout perfs = new VerticalLayout();
        perfs.setMargin(true);
        perfs.setSizeFull();

        // Création de chaque label pour le récapitulatif des performances
        Label nomPerfs = new Label();
        nomPerfs.setContentMode(Label.CONTENT_XHTML);
        nomPerfs.setValue("<b>Performances : ");
        perfs.addComponent(nomPerfs);
        perfs.addComponent(new Label("&nbsp;", Label.CONTENT_XHTML));

        Label nbHaussePerf = new Label();
        nbHaussePerf.setContentMode(Label.CONTENT_XHTML);
        nbHaussePerf
                .setValue("<font color=\"green\">Nombre de hausses de performance : "
                        + nbHausse + "</font>");
        nbHaussePerf.setImmediate(true);
        perfs.addComponent(nbHaussePerf);
        perfs.addComponent(new Label("&nbsp;", Label.CONTENT_XHTML));

        Label nbBaissePerf = new Label();
        nbBaissePerf.setContentMode(Label.CONTENT_XHTML);
        nbBaissePerf
                .setValue("<font color=\"red\">Nombre de baisses de performance : "
                        + nbPerte + "</font>");
        nbBaissePerf.setImmediate(true);
        perfs.addComponent(nbBaissePerf);

        layout.addComponent(perfs);
        setCompositionRoot(layout);

    }
}
