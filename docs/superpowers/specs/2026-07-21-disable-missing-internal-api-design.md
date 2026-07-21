# Disable Missing Internal API Feature Chain

## Context

The backend baseline currently contains a feature chain that imports classes from
internal packages that are not available in the current dependency set:

- `com.siact.api.common.*`
- `com.siact.api.feign.*`
- `com.siact.ins.*`

The affected code is concentrated in the `sec` controllers, services, DTOs,
converters, utility code, and the TDengine data service. Maven can now resolve
the project dependencies, but compilation stops on these missing internal API
types.

## Decision

Comment out the complete affected Java source files, preserving their original
contents for a later restoration. The disabled set is:

- `sec/controller/DevController.java`
- `sec/controller/PropController.java`
- `sec/controller/SecInsController.java`
- `sec/convertor/ClassConvertor2DTO.java`
- `sec/dto/PropValDTO.java`
- `sec/sevice/DataService.java`
- `sec/sevice/DevService.java`
- `sec/sevice/PropInsService.java`
- `sec/sevice/SecInsService.java`
- `sec/sevice/impl/DataServiceImpl.java`
- `sec/sevice/impl/DevServiceImpl.java`
- `sec/sevice/impl/PropInsServiceImpl.java`
- `sec/sevice/impl/SecInsServiceImpl.java`
- `sec/utils/SiactSecApiFeignUtil.java`
- `tdengine/service/TaosDataService.java`
- `tdengine/service/TaosDataServiceImpl.java`

Each file will remain in place and every source line will be line-commented so
the source can be restored without reconstructing the implementation. No POM,
Nacos, database, or unrelated user changes will be modified.

## Runtime Impact

The following routes and capabilities will be unavailable until the internal
API dependencies are restored:

- `/api/dev`
- `/api/prop`
- `/api/ins`
- data query operations backed by the disabled services
- TDengine data service operations that use the disabled internal query types

The remaining authentication, system, Redis, Nacos, MQTT, WebSocket, and other
baseline code will remain untouched.

## Validation

After the comments are applied:

1. Run `mvn -q -DskipTests compile` in `hydrocore-be`.
2. Run the existing Maven test suite.
3. Confirm no active source imports `com.siact.api.*` or `com.siact.ins.*`.
4. Confirm the current unrelated worktree changes are preserved.

The expected result is a compilable baseline with the internal-API feature chain
explicitly disabled.
