#   FiskeFinner

**IN2000 Vår 2025 - Team-17**


> This README is mostly for developers and others who want a deeper insight into our project.

##   **FiskeFinner – Din AI-baserte fiskeguide**

## Introduction

FiskeFinner er en Android-app utviklet i Kotlin som viser anbefalte fiskesteder i Norge på et interaktivt kart, basert på værdata og maskinlæring. Appen kombinerer Mapbox for kart, værprognoser og en TensorFlow Lite-modell for å gi prediksjoner på fiskeforhold. Brukeren kan søke etter steder, velge fisketype og planlegge fisketurer basert på AI-vurderinger.

## Visuals

<table>
  <tr>
    <td><img src="https://github.uio.no/IN2000-V25/team-17/assets/10476/58e9a307-9f2d-48d7-bafd-0da8fc90050c" width="250"/></td>
    <td><img src="https://github.uio.no/IN2000-V25/team-17/assets/10476/633a8b8e-20ee-491a-9cbe-905b717f084f" width="250"/></td>
    <td><img src="https://github.uio.no/IN2000-V25/team-17/assets/10476/2d137be9-5d0c-4044-9dd1-8e1c3e16a71f" width="250"/></td>
  </tr>
  <tr>
    <td><img src="https://github.uio.no/IN2000-V25/team-17/assets/10476/49a450b7-9d86-4dc9-81a1-08c534acb324" width="250"/></td>
    <td><img src="https://github.uio.no/IN2000-V25/team-17/assets/10476/e3fb9da5-b455-4e9a-81f5-4665988867d4" width="250"/></td>
    <td><img src="https://github.uio.no/IN2000-V25/team-17/assets/10476/7d1ab270-0375-4188-a4eb-a4acb1ffa81f" width="250"/></td>
  </tr>
  <tr>
    <td><img src="https://github.uio.no/IN2000-V25/team-17/assets/10476/3eef2b06-4bef-41ab-8661-ccf261357af9" width="250"/></td>
    <td><img src="https://github.uio.no/IN2000-V25/team-17/assets/10476/94668ea4-f6ec-4dc8-ac8b-9cd0083d6e70" width="250"/></td>
    <td><img src="https://github.uio.no/IN2000-V25/team-17/assets/10476/8bdee5c3-8b83-4300-b2c8-8d6c9ca63532" width="250"/></td>
  </tr>
</table>



## Key Features

*  Søk etter steder i Norge via Mapbox
*  Kartvisning med pins for lokasjoner
*  AI-basert prediksjon av fiskeforhold
*  Filter basert på valgt fisketype
*  Dynamisk integrering av værdata
*  Enkelt navigasjonsoppsett (Dashboard, Kart, Fisketyper, Profil)
*  Asynkrone operasjoner med Kotlin Coroutines og StateFlow

##  Documentation and Architecture

Se egne filer:

* `ARCHITECTURE.md` – detaljer om MVVM-oppsettet
* `MODELING.md` – detaljer om AI-modellen og databehandling
* `MAPBOX_SETUP.md` – hvordan vi bruker Mapbox Search API

##  Libraries and Frameworks Used

* **Jetpack Compose** – moderne UI-bygging
* **Mapbox SDK + Search API** – kart og søkefunksjon
* **Ktor Client** – API-kall mot Mapbox og værdata
* **TensorFlow Lite** – for lokal AI-modell på mobil
* **Kotlin Coroutines / Flow** – for asynkron databehandling
* **Material Design** – UI-komponenter
* **Coil** – bildehåndtering
* **AndroidX** – moderne Android-støttebiblioteker
* **JUnit / Espresso** – testing

##  Installation

**Krav:**

* Android Studio Hedgehog (eller nyere)
* Min SDK: API 26 (Android 8.0)
* Internett-tilgang
* Lokasjonstillatelse: `ACCESS_FINE_LOCATION`

###  Steg-for-steg:

1. Klon repoet:

   ```bash
   git clone https://github.uio.no/IN2000-V25/team-17.git  
   cd team-17  
   ```
2. Åpne prosjektet i Android Studio

3. Kjør appen på fysisk enhet eller emulator

## Bruk på fysisk Android-enhet

### Koble til via USB:

* Aktiver utviklermodus og USB-feilsøking på telefonen
* Koble telefonen til PC
* Kjør appen fra Android Studio

### Koble via Wi-Fi (ADB):

* I Android Studio: `Pair devices using Wi-Fi`
* Følg instruksjoner (QR eller kode)
* Kjør appen

## AI-modell

Vi bruker en trenet TensorFlow Lite-modell som klassifiserer fiskeplasser i klasser (1–4) basert på:

* Fisketype
* Lokasjon (lat/lon)
* Værforhold (vind, temperatur, trykk m.m.)

Ratingen vises i UI og brukes som grunnlag for anbefaling.

##  Testing

* Manuell testing i emulator og på fysisk enhet
* JUnit-test for ViewModel-logikk
* Espresso kan benyttes for UI-testing

##  API-er brukt

* Mapbox Search API
* MET.no (yr.no) vær-API
* Egentrenet AI-modell via TensorFlow Lite
* MittFiske API
* FishBuddy API


##  Bidragsytere

* Sigurd Nordbye – \[[sigurnor@uio.no](mailto:sigurnor@uio.no)]
* Sara Aadahl – \[[saraaad@ifi.uio.no](mailto:saraaad@ifi.uio.no)]
* Hedda Nord Holmgren – \[[heddanh@ifi.uio.no](mailto:heddanh@ifi.uio.no)]
* Marie Helene Hansen – \[[marihhan@ifi.uio.no](mailto:marihhan@ifi.uio.no)]
* Daniel Shahzad-Landsverk – \[[danishah@ifi.uio.no](mailto:danishah@ifi.uio.no)]
* Oscar Lyckander – \[[oscar.lyckander@usit.uio.no](mailto:oscar.lyckander@usit.uio.no)]
 

## Lisens / Bruk

Dette prosjektet er utviklet som en del av IN2000 ved Universitetet i Oslo. Ikke ment for kommersiell bruk.
