# Virtual Horse Race

La applicacion en tiempo de ejecucion escoge si usar Threads tradicionales o virtual Threads basado en la cantidad de caballos, si el numero es mayor a 10, usa virtual Threads para mayor escalabilidad con muchos caballos.

Y si el numero de caballos es menor a 10, usa Threads tradicionales para menor overhead con pocos caballos.

In the project directory, you can run:

### `mvn spring-boot:run`
