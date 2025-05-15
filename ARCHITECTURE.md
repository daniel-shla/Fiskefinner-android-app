# **Introduksjon**
Denne appen lar deg se definerte fiskesteder på et interaktivt kart, prediksjon av fiskeforhold ved et gitt fiskested, dagens vær, og dine planlagte fisketurer. Formålet er å gjøre det enklere å planlegge en fisketur, samt få fisk på turen. Målgruppen er hobbyfiskere, og folk ellers som vil prøve fiskelykken.

## **Overordnet arkitektur**
Appen bruker MVVM-arkitektur, med en klar separasjon mellom Model, View og ViewModel. Mappestrukturen er delt i data/ for Model-komponenten, og screens/ for View- og ViewModel-komponentene i arkitekturen. For en enkelt skjerm og tilhørende ViewModel har vi screens/[skjerm-navn]/, for eksempel screens/dashboard/. I tillegg har vi en ml/ for maskinlæringsmodellen og dens ViewModel, da output fra modellen brukes for å avgjøre komponenter på ulike skjermer, men modellen i seg selv ikke er noe som vises.
På grunn av hvordan MittFiske-datakilden er bygd opp, så har vi i tillegg mittFiske/ med MittFiskeViewModel og MittFiskeViewModelFactory i screens/.

## **Objektorienterte prinsipper og designmønstre**
Vi bruker Repository pattern med inndeling i datasource-, dataclass-, og repository-filer for hvert API eller datakilde vi bruker. I tillegg bruker vi Hilt dependency injection, så vi sikrer lav kobling mellom data- og presentasjonslaget. Hver ViewModel håndterer kun logikk for sin tilhørende View, som sikrer høy kohesjon.

## **Tech stack**
Appen er skrevet i Kotlin, med Jetpack Compose for UI, og Ktor for API-kall. Vi bruker Hilt for dependency injection, og Kotlin Coroutines og Kotlin Flow for asynkron databehandling.

## **Android API level**
Vi har valgt API level 26 som minimum, for å kunne bruke moderne biblioteker og funksjoner i utviklingen av appen, samtidig som vi fortsatt støtter en stor andel aktive enheter.

## **Vedlikehold og videreutvikling**
For å legge til en ny skjerm, følg mønsteret FeatureXScreen, FeatureXViewModel, og legg eventuelt til nytt til data-komponenten i data/ med mønsteret APInavnDC i dataClasses/, APInavnDataSource i source/, og APInavnRepository i repository/.
