# Implementation Plan: 'ফার্মেসি খাতা' — Barcode Camera Scanning & Real-Time Firebase Cloud Sync

This plan details the architecture and roadmap for upgrading **'ফার্মেসি খাতা' (Pharmacy Khata)** with camera-based barcode scanning for rapid POS counter billing and real-time cloud data synchronization across multiple devices using Firebase Firestore and Google Sign-In.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following core enhancements have been selected and prioritized based on your feedback:

- **Confirmed Decision 1: Camera Barcode Scanning in POS**: We will integrate an in-app viewfinder scanner using CameraX and ML Kit / ZXing barcode analyzer. Scanning any medicine box barcode will instantly search, validate safety (not expired, stock > 0), and add it to the active POS cart in milliseconds.
- **Confirmed Decision 2: Real-Time Multi-Device Cloud Sync**: We will provision and integrate Firebase Firestore with Google Sign-In via Credential Manager, allowing multiple pharmacy counters, branches, and mobile phones to synchronize stock, sales, and ledgers in real-time.
- **Recommended Architectural Approach (Offline-First Hybrid Cache)**: In Bangladesh pharmacy settings, broadband or mobile data may occasionally drop. We recommend keeping the local responsive reactive cache with Firestore offline persistence enabled, ensuring transactions and billing never halt even when internet connectivity is intermittent.

---

## 1. Overview & Core Concept

- **What It Does**: 
  1. **High-Speed POS Barcode Billing**: Pharmacists tap a quick scan button on the POS screen, aim the camera at medicine barcodes or strip packaging, and the app automatically identifies the medicine, checks expiry and stock limits, and increments the cart.
  2. **Multi-Device & Multi-Staff Sync**: The owner and employees can operate multiple Android devices (e.g. tablet at billing counter, mobile phone in the storage room/warehouse) with synchronized inventory, purchase records, and customer dues.
- **Target Audience**: Retail pharmacies, medicine store managers, and hospital pharmacy counters operating single or multiple branches.
- **Key Value**: Eliminates manual medicine search typing at busy counters, prevents inventory discrepancies across devices, and ensures data is safe against device loss or damage.

---

## 2. User Experience & Visual Design

- **Key User Flows**:
  - **Flow 1: One-Tap Barcode Checkout**:
    1. Pharmacist opens the POS screen and taps the camera scan icon next to the search bar.
    2. A sleek semi-transparent camera overlay with an illuminated targeting rect and green scan beam opens.
    3. Detecting a valid barcode plays a crisp haptic/audio confirmation and immediately adds the item to the cart.
    4. If the scanned medicine is expired or has zero stock, a prominent red safety toast and sound alert prevents entry.
  - **Flow 2: Google Sign-In & Multi-Device Sync**:
    1. On first launch or from Settings, pharmacist signs in with Google via the Credential Manager bottom sheet.
    2. The app syncs the pharmacy branch profiles, medicine catalog, and customer ledgers from Firestore.
    3. Changes made on Counter A (e.g., selling 2 strips of Napa Extra) immediately update available stock on Counter B.

- **Visual Identity & Theme**:
  - *Color Scheme*: Medical Teal (`#00796B`, `#004D40`), Clean White (`#FFFFFF`), Soft Mint Container (`#E0F2F1`), Alert Red (`#D32F2F`), and Dark Mode surface depth (`#121A19`).
  - *Typography*: **Anek Bangla** for Bengali labels and navigation; **IBM Plex Mono** for barcodes, numeric quantities, and thermal receipts.
  - *Scanner UI*: Full-bleed CameraX Compose overlay (`AndroidView` preview) with rounded viewfinder framing, flashlight toggle, and manual search fallback.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Barcode Analyzer Engine (CameraX + ML Kit / ZXing)**:
  - *Chosen Approach*: Lightweight ML Kit Barcode Scanning with CameraX `ImageAnalysis`.
  - *Why*: Sub-100ms detection rate even on budget Android devices and low-light pharmacy shelves.
  - *Alternative Considered*: External USB hardware scanners. *Trade-off*: External hardware adds peripheral costs, whereas camera scanning works directly on any Android smartphone.

- **Decision 2: Cloud Sync Strategy (Firestore Real-Time Snapshots + Credential Manager)**:
  - *Chosen Approach*: Firebase Firestore collection sub-trees scoped under `/pharmacies/{pharmacyId}/...` with `snapshot()` Flow listeners and Google Sign-In via `GetSignInWithGoogleOption`.
  - *Why*: Provides zero-latency real-time state updates, automated offline caching, and zero-trust security rules tied to authenticated Google UIDs.
  - *Alternative Considered*: Periodic manual JSON backup upload. *Trade-off*: Manual backups fail when employees forget, while Firestore syncs automatically on every transaction.

---

## 4. Technical Architecture & Data Strategy

```
┌─────────────────────────────────────────────────────────────┐
│                    Jetpack Compose UI                       │
│  ┌──────────────┐ ┌───────────────┐ ┌────────────────────┐  │
│  │ Dashboard    │ │ POS & Scanner │ │ Khata & Inventory  │  │
│  └──────┬───────┘ └───────┬───────┘ └─────────┬──────────┘  │
└─────────┼─────────────────┼───────────────────┼─────────────┘
          │                 ▼                   │
┌─────────▼─────────────────────────────────────▼─────────────┐
│                     PharmacyViewModel                       │
│  - Reactive Cart State  - Strict Expiry & Stock Blocker     │
│  - Barcode Resolution   - Customer / Ledger Coordinators    │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│              PharmacyRepository (Sync Engine)               │
│  ┌─────────────────────────────┐ ┌───────────────────────┐  │
│  │ Local Cache & Offline State │ │ Firestore Sync Engine │  │
│  │ (Instant UI StateFlows)     │ │ (Snapshot Listeners)  │  │
│  └──────────────┬──────────────┘ └───────────┬───────────┘  │
└─────────────────┼────────────────────────────┼──────────────┘
                  ▼                            ▼
          ┌──────────────┐            ┌─────────────────┐
          │ Device Disk  │            │ Firebase Cloud  │
          │ Cache (JSON) │            │ Firestore DB    │
          └──────────────┘            └─────────────────┘
```

### Data Model & Collection Hierarchy:
```
/pharmacies/{pharmacyId}
   ├── medicines/{medicineId}       (Brand, Generic, Expiry MM/YY, Batch, Stock, Barcode)
   ├── sales/{saleId}               (Customer, Item List, Grand Total, Due, Payment Mode)
   ├── purchases/{purchaseId}       (Supplier, Invoice No, Stock In, Amount, Paid)
   ├── customers/{customerId}       (Name, Phone, Total Due, Address)
   ├── suppliers/{supplierId}       (Company, Phone, Total Payable)
   ├── expenses/{expenseId}         (Category, Amount, Date)
   └── cashTransactions/{txId}      (Type IN/OUT, Amount, Reason)
```

### State & Execution Mapping:
1. **Camera Analyzer (`BarcodeScannerSheet.kt`)**: Analyzes preview frames -> extracts barcode raw value -> queries `medicines.value.find { it.barcode == scannedCode }` -> performs `addToCart(medicine)` -> triggers haptic feedback.
2. **Firestore Repository (`FirestorePharmacyRepository.kt`)**: Authenticates Google user -> registers `collection(...).snapshots()` -> updates in-memory StateFlows -> handles optimistic offline writes with `.await()`.
3. **Strict Validation Invariant**: Even in barcode scanning mode, any item with `isExpired() == true` or `currentStockPieces <= 0` triggers an immediate error dialog and cannot enter the cart.
