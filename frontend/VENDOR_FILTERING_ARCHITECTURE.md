# Vendor Active/Inactive Status Filtering - Architecture Diagram

## Component Hierarchy

```
MainLayout
  └── Procurement Workspace Page
       ├── Purchase List Page
       │   └── Table (Purchase records)
       │
       └── Vendor List Page ✨ ENHANCED
           ├── Header
           │   ├── Title: "Vendors"
           │   └── [+ Create Vendor] button
           │
           ├── Filters Section ✨ NEW
           │   ├── Search Input
           │   ├── Status Toggle ✨ NEW
           │   │   ├── [All]
           │   │   ├── [Active] ← Default
           │   │   └── [Inactive]
           │   └── [Clear Filters] button
           │
           ├── Table Component
           │   └── Vendor List (active: boolean)
           │       ├── Name
           │       ├── Phone
           │       ├── Address
           │       ├── Notes
           │       └── Actions [View] [Edit] [Delete]
           │
           ├── Pagination
           │   └── Prev / Next / Page Size
           │
           └── Delete Modal ✨ ENHANCED
               ├── Confirmation message
               └── [Cancel] [Delete] buttons

VendorDetailsPage ✨ ENHANCED
  ├── Header
  │   ├── [← Back] button
  │   ├── Title: "Vendor Details #42"
  │   ├── Status Badge ✨ NEW
  │   │   ├── 🟢 Green dot (if active)
  │   │   ├── ⚪ Gray dot (if inactive)
  │   │   └── Text: "Active" / "Inactive"
  │   └── [Edit] [Delete] buttons
  │
  ├── Vendor Information Card
  │   ├── Name
  │   ├── Phone
  │   ├── Address
  │   └── Notes (if exists)
  │
  └── Purchases Section
      └── Table of vendor's products
```

---

## State Management Flow

```
┌─────────────────────────────────────────────────────────┐
│ VendorListPage Component                                │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  State Variables:                                       │
│  ┌──────────────────────────────────────────────────┐  │
│  │ searchTerm: ""                                   │  │
│  │ statusFilter: "active"  ← DEFAULT               │  │
│  │ page: 0                                          │  │
│  │ size: 10                                         │  │
│  │ isDeleteModalOpen: false                         │  │
│  │ deleteId: null                                   │  │
│  └──────────────────────────────────────────────────┘  │
│                                                         │
│  ↓ User Interactions                                    │
│                                                         │
│  [Status Button Click]                                  │
│      ↓                                                  │
│  setStatusFilter('inactive')                            │
│      ↓                                                  │
│  filters = useMemo → recalculated                       │
│      ↓                                                  │
│  useVendors(filters) → dependency triggered            │
│      ↓                                                  │
│  vendorApi.getAll(filters)                             │
│      ↓                                                  │
│  GET /api/vendors?status=inactive&page=0&size=10       │
│      ↓                                                  │
│  Response ← Page<Vendor>                               │
│      ↓                                                  │
│  setVendors(response.content)                          │
│      ↓                                                  │
│  <Table> re-renders with inactive vendors              │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

---

## Memoization Strategy

```
┌────────────────────────────────────────────┐
│ useMemo Dependencies                       │
├────────────────────────────────────────────┤
│                                            │
│  filters = useMemo(() => ({               │
│    page,              ↙ Pagination state   │
│    size,              ↙ Pagination state   │
│    search,            ↙ Search debounced   │
│    status,            ↙ Filter toggle      │
│  }), [page, size, debouncedSearch,        │
│       statusFilter])                       │
│                                            │
│  ↓ Changes to ANY dependency               │
│                                            │
│  → filters object re-created               │
│  → useVendors hook dependency triggered    │
│  → fetchVendors callback fires             │
│  → API call with new params                │
│                                            │
│  columns = useMemo(() => [                │
│    {...},             ↙ t (translations)   │
│    {...},             ↙ tc (common i18n)   │
│    {...},             ↙ navigate fn        │
│    {...},             ↙ confirmDelete fn   │
│  ], [t, tc, navigate,                     │
│       deleteLoading, confirmDelete])      │
│                                            │
│  ↓ All button callbacks use latest refs    │
│                                            │
│  → Delete button click captured correctly  │
│  → Modal opens/closes on demand            │
│                                            │
└────────────────────────────────────────────┘
```

---

## Delete Confirmation Flow

```
┌────────────────────────────────────────────────────────┐
│ Delete Flow with Modal                                 │
├────────────────────────────────────────────────────────┤
│                                                        │
│  1. User clicks [Delete] button on vendor row          │
│     ↓                                                  │
│  2. confirmDelete(vendorId) called                     │
│     ↓                                                  │
│  3. setItemId(vendorId)                                │
│  4. setIsModalOpen(true)                               │
│     ↓                                                  │
│  5. Modal appears with confirmation message             │
│     ┌──────────────────────────────┐                   │
│     │ Are you sure you want to     │                   │
│     │ delete this vendor?          │                   │
│     │                              │                   │
│     │ This vendor will be marked   │                   │
│     │ as inactive.                 │                   │
│     │                              │                   │
│     │ [Cancel] [Delete]            │                   │
│     └──────────────────────────────┘                   │
│     ↓                                                  │
│  6a. User clicks [Cancel]                              │
│      ↓                                                 │
│      handleCloseModal()                                │
│      → setIsModalOpen(false)                           │
│      → setItemId(null)                                 │
│      ↓                                                 │
│      Modal closes, nothing happens                     │
│                                                        │
│     OR                                                 │
│                                                        │
│  6b. User clicks [Delete]                              │
│      ↓                                                 │
│      handleDelete(deleteVendor)                        │
│      ↓                                                 │
│      deleteVendor(itemId)  [API call]                  │
│      ↓                                                 │
│      DELETE /api/vendors/{id}                          │
│      ↓                                                 │
│      Response: Success                                 │
│      ↓                                                 │
│      toast.success(t('deleted'))  [Toast notification] │
│      ↓                                                 │
│      onDeleted() → refetch()  [Re-fetch vendor list]   │
│      ↓                                                 │
│      setIsModalOpen(false)                             │
│      setItemId(null)                                   │
│      ↓                                                 │
│      Modal closes                                      │
│      Vendor list updates (vendor no longer shown)      │
│                                                        │
└────────────────────────────────────────────────────────┘
```

---

## API Contract

### Request

```
GET /api/vendors?page=0&size=10&status=active

Query Parameters:
  page: 0          (current page, 0-indexed)
  size: 10         (records per page)
  search: "tech"   (optional, text search)
  status: "active" (optional, 'active'|'inactive'|'all')
```

### Response

```json
{
  "content": [
    {
      "id": 1,
      "name": "TechVendor Inc.",
      "phone": "+966123456789",
      "address": "Riyadh, KSA",
      "notes": "Quality supplier",
      "active": true
    },
    {
      "id": 2,
      "name": "Office Supplies Co.",
      "phone": "+966987654321",
      "address": "Jeddah, KSA",
      "notes": null,
      "active": false
    }
  ],
  "totalElements": 25,
  "totalPages": 3,
  "size": 10,
  "number": 0,
  "first": true,
  "last": false,
  "empty": false
}
```

---

## UI State Machine

```
VendorListPage States:

┌────────────────────────────────────────┐
│ INITIAL                                 │
├────────────────────────────────────────┤
│ statusFilter: "active"                  │
│ searchTerm: ""                          │
│ loading: true                           │
│ isDeleteModalOpen: false                │
└────────────────────────────────────────┘
         ↓ (on mount, useEffect fires)
┌────────────────────────────────────────┐
│ LOADING (fetching vendors)              │
├────────────────────────────────────────┤
│ Table shows skeleton/spinner            │
│ User can change filters (state updates) │
└────────────────────────────────────────┘
         ↓ (API responds)
┌────────────────────────────────────────┐
│ SUCCESS (vendors loaded)                │
├────────────────────────────────────────┤
│ vendors: Vendor[]                       │
│ error: null                             │
│ Table renders real data                 │
│ User can interact (delete, edit, etc.)  │
└────────────────────────────────────────┘
         ↓ (user clicks delete)
┌────────────────────────────────────────┐
│ DELETE_CONFIRMING                       │
├────────────────────────────────────────┤
│ isDeleteModalOpen: true                 │
│ deleteId: <number>                      │
│ Modal visible                           │
└────────────────────────────────────────┘
    ↙               ↘
[Cancel]           [Delete]
    ↓                 ↓
┌────────┐    ┌──────────────────┐
│ SUCCESS│    │ DELETE_LOADING   │
│(revert)│    │ deleteLoading:   │
└────────┘    │   true           │
              │ Delete button    │
              │   disabled       │
              └──────────────────┘
                      ↓ (API responds)
              ┌──────────────────┐
              │ SUCCESS          │
              │ Toast notif      │
              │ refetch()        │
              │ List updated     │
              └──────────────────┘

┌────────────────────────────────────────┐
│ ERROR (API fails)                       │
├────────────────────────────────────────┤
│ error: "Network error..."               │
│ vendors: [] (empty)                     │
│ Shows error message + [Retry] button    │
└────────────────────────────────────────┘
         ↓ (user clicks retry)
┌────────────────────────────────────────┐
│ LOADING (retry)                         │
└────────────────────────────────────────┘
```

---

## Filter Combination Logic

```
Search + Status Filter Work Together:

┌─────────────────────────────────────┐
│ Filter Combinations                  │
├─────────────────────────────────────┤
│                                     │
│ Search: "" + Status: "active"       │
│   → All active vendors              │
│                                     │
│ Search: "tech" + Status: "active"   │
│   → Active vendors matching "tech"  │
│                                     │
│ Search: "tech" + Status: "inactive" │
│   → Inactive vendors matching "tech"│
│                                     │
│ Search: "" + Status: "all"          │
│   → All vendors (active + inactive) │
│                                     │
│ Search: "tech" + Status: "all"      │
│   → All vendors matching "tech"     │
│                                     │
└─────────────────────────────────────┘

Memoized Filters:
  const filters = useMemo(() => ({
    page,
    size,
    ...(debouncedSearch && { search: debouncedSearch }),
    status: statusFilter,
  }), [...])

  ↑ Conditional spread ensures search is only
    included if it has a value
```

---

## Translation Keys Structure

```
vendor.json (Namespaced translations)
├── title: "Vendors"
├── createNew: "Create Vendor"
├── searchPlaceholder: "Search..."
│
├── filterActive: "Active"         ✨ NEW
├── filterInactive: "Inactive"     ✨ NEW
├── filterAll: "All"               ✨ NEW
│
├── status: {                      ✨ NEW
│   ├── active: "Active"
│   └── inactive: "Inactive"
├── }
│
├── columns: {...}
├── form: {...}
├── pagination: {...}
├── details: {...}
├── messages: {...}
│
└── deleteConfirmMessage: "..."    ✨ NEW (Enhanced)
```

---

## CSS Class Naming (BEM Convention)

```
Block: .vendor-list__
  ├── Element: header
  │   └── Modifier: (none)
  ├── Element: title
  ├── Element: filters
  │   └── Modifier: (none)
  ├── Element: search
  ├── Element: status-toggle      ✨ NEW
  ├── Element: status-btn         ✨ NEW
  │   └── Modifier: --active      ✨ NEW
  ├── Element: table
  ├── Element: actions
  ├── Element: pagination
  │   └── Sub-elements: ...

Block: .vendor-details__
  ├── Element: header
  │   ├── Element: title
  │   └── Element: badge          ✨ NEW
  │       ├── Modifier: --active  ✨ NEW
  │       └── Modifier: --inactive ✨ NEW
  ├── Element: badge-dot          ✨ NEW
  └── Element: ...
```

---

## Mobile Responsive Breakpoints

```
Desktop (1024px+):
  ┌──────────────────────────────────────────┐
  │ Vendors                    [+ Create]    │
  ├──────────────────────────────────────────┤
  │ Search: [____] [All][Active][Inactive]   │
  │                            [Clear]       │
  ├──────────────────────────────────────────┤
  │ Table with all columns visible           │
  └──────────────────────────────────────────┘

Tablet (768px - 1023px):
  ┌─────────────────────────────────┐
  │ Vendors           [+ Create]    │
  ├─────────────────────────────────┤
  │ Search: [___________]           │
  │ [All][Active*] [Inactive]       │
  │ [Clear Filters]                 │
  ├─────────────────────────────────┤
  │ Table (simplified columns)      │
  └─────────────────────────────────┘

Mobile (< 768px):
  ┌────────────────────────┐
  │ Vendors [+]            │
  ├────────────────────────┤
  │ Search: [__________]   │
  │ [All] [Active*]        │
  │ [Inactive] [Clear]     │
  ├────────────────────────┤
  │ Table (stacked cards)  │
  └────────────────────────┘
```

---

## Testing Paths

```
Happy Path (Success):
  1. Load page → vendors visible
  2. Click [Active] → only active vendors shown
  3. Click [Inactive] → only inactive vendors shown
  4. Click [All] → all vendors shown
  5. Type in search → filters apply with status
  6. Click [Delete] → modal appears
  7. Confirm delete → vendor removed, list updates
  8. Click [Clear] → resets to active, search clears
  9. Switch language → all labels translate
  10. View vendor detail → status badge shows correctly

Error Scenarios:
  - Network error → Error message + [Retry]
  - 404 Not Found → Show appropriate error
  - Server error → Toast notification
  - Invalid filter → Default to "active"
```

---

## Performance Optimization

```
Memoization Strategy:

┌──────────────────────────────┐
│ useVendors Hook              │
├──────────────────────────────┤
│ ✓ filters memoized           │
│ ✓ Only fetches on filter     │
│   change (via useEffect)     │
│ ✓ Debounced search (400ms)   │
│ ✓ Prevents unnecessary       │
│   re-renders                 │
└──────────────────────────────┘

┌──────────────────────────────┐
│ Table Columns                │
├──────────────────────────────┤
│ ✓ Memoized array             │
│ ✓ Includes all event handlers│
│ ✓ Dependencies tracked       │
│ ✓ Callbacks don't re-create  │
│   unless needed              │
└──────────────────────────────┘

┌──────────────────────────────┐
│ Pagination Info              │
├──────────────────────────────┤
│ ✓ Computed from response     │
│ ✓ No extra API calls         │
│ ✓ Calculated once per render │
└──────────────────────────────┘
```

This architecture ensures:
- ✓ Minimal re-renders
- ✓ Efficient data fetching
- ✓ Smooth user interactions
- ✓ Responsive UI updates

