## Data pipeline (with the **contract classes** explicitly included)

### 1) **Frontend → Backend (HTTP request)**
**Frontend payloads (contracts)**
- `CreateCompanyRequest` *(JSON body for POST)*
- `UpdateCompanyRequest` *(JSON body for PUT update)*
- `@PathVariable Long id` *(for update/delete/deactivate; deactivate has no body)*

⬇️

### 2) **Web adapter layer**
**`CompanyCommandController`**
- Receives request contracts (`CreateCompanyRequest`, `UpdateCompanyRequest`, `id`)
- Converts request → **domain** (`Company.createNew(...)` for create)
- Calls the application port (**`CompanyCommandPort`**)
- Maps **domain → response contract** using **`CompanyResponseMapper`**
- Wraps response in **`ApiResponseDto<?>`** *(contract envelope)*

⬇️

### 3) **Application layer**
**`CompanyCommandPort` (inbound port / interface)**  
⬇️  
**`CompanyCommandService` (use case implementation)**
- Applies business rules
- Uses domain methods (`updateDetails`, `deactivate`, `markDeleted`)
- Calls persistence via **`CompanyRepositoryPort`**

⬇️

### 4) **Persistence boundary**
**`CompanyRepositoryPort` (outbound port / interface)**  
⬇️  
**`CompanyRepositoryAdapter` (persistence adapter)**
- Calls **`CompanySpringDataRepository`** (Spring Data JPA)
- Uses **`CompanyPersistenceMapper`** to translate:
    - `CompanyJpaEntity` ⇄ `Company` (domain)

⬇️

### 5) **Infrastructure / Data layer**
**`CompanySpringDataRepository`** (`JpaRepository<CompanyJpaEntity, Long>`)  
⬇️  
**Database** (table `companies`)

---

## Contract classes summary (HTTP boundary)
### **Request contracts**
- `CreateCompanyRequest`
- `UpdateCompanyRequest`

### **Response contracts**
- `CompanyResponseDto`
- `ApiResponseDto<T>` *(standard response wrapper)*

### **Behavior/status contract**
- `SuccessCode` / `ErrorCode` (via `CodeDescriptor`) determine `ApiResponseDto` fields like status code + message key.