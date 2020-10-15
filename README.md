# Quasar-fuego

Quasar-fuego es una aplicación que permite obtener la localización del satellite y descifrar los mensajes recibidos para que sean legibles.

## Instalación


La aplicación se encuentra desplegada en una azure function, por medio de maven se utiliza el artefacto y se reailza el despliegue, y se activa cuando se realiza una petición HTTP

En el directorio de los archivos del aplicativo ejecutar los siguientes comandos si se desea ejecutar localmente

```bash
mvn clean -DskipTests package
mvn azure-functions:run
```

## Uso

```bash
curl --location --request POST 'https://jdmm-quasar.azurewebsites.net/api/topsecret' \
--header 'Content-Type: application/json' \
--data-raw '{
    "satellites":[
        {
            "name":"kenobi",
            "distance":100.0,
            "message":["","mundo"]
        },
        {
            "name":"skywalker",
            "distance":115.5,
            "message":["hola",""]
        },
        {
            "name":"sato",
            "distance":142.7,
            "message":["",""]
        }
    ]  
}
```

## Contribucion
Pueden solicitar permisos a https://github.com/jmartine2319/quasar-fuego.git a enviar un correo a jmartinezopg@gmail.