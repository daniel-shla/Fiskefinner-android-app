#   FiskeFinner

**IN2000 Vår 2025 - Team-17**


> This README is mostly for developers and others who want a deeper insight into our project.

##   **FiskeFinner – Din AI-baserte fiskeguide**

## Introduction

FiskeFinner er en Android-app utviklet i Kotlin som viser anbefalte fiskesteder i Norge på et interaktivt kart, basert på værdata og maskinlæring. Appen kombinerer Mapbox for kart, værprognoser og en TensorFlow Lite-modell for å gi prediksjoner på fiskeforhold. Brukeren kan søke etter steder, velge fisketype og planlegge fisketurer basert på AI-vurderinger.

## Visuals
<table>
  <tr>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/82f1a2e8-f6ea-4014-9f3d-85b8e4e42bc4" width="200"/><br/>
      <b>Screen 9</b>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/fbe6d879-e429-42f5-8d15-a9ed30c3ebab" width="200"/><br/>
      <b>Screen 8</b>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/27d2173c-fe0f-4e03-addd-daf8692d38fb" width="200"/><br/>
      <b>Screen 7</b>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/39b128d4-09b5-455b-9495-7f8d3dfcb2d7" width="200"/><br/>
      <b>Screen 6</b>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/c82e3435-9205-4314-89b7-68e4cf5eaab2" width="200"/><br/>
      <b>Screen 5</b>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/3308ea55-3935-4d51-afa3-0da811b1a564" width="200"/><br/>
      <b>Screen 4</b>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/3a621559-d614-455d-84e7-9cfdfd7252fb" width="200"/><br/>
      <b>Screen 3</b>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/158ded64-0a22-40ba-867c-b1af13394553" width="200"/><br/>
      <b>Screen 2</b>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/41f78184-2b45-4dac-91ad-90abb901c688" width="200"/><br/>
      <b>Screen 1</b>
    </td>
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
