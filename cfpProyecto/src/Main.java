import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Clase que representa un Producto en el sistema.
 */
class Producto {
    private String id;
    private String nombre;
    private double precio;
    private int cantidadVendida;

    public Producto(String id, String nombre, double precio) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.cantidadVendida = 0;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public double getPrecio() { return precio; }
    public int getCantidadVendida() { return cantidadVendida; }
    
    public void agregarVenta(int cantidad) {
        this.cantidadVendida += cantidad;
    }
}

/**
 * Clase que representa un Vendedor en el sistema.
 */
class Vendedor {
    private String tipoDocumento;
    private String numeroDocumento;
    private String nombres;
    private String apellidos;
    private double ventasTotales;

    public Vendedor(String tipoDocumento, String numeroDocumento, String nombres, String apellidos) {
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.ventasTotales = 0.0;
    }

    public String getNumeroDocumento() { return numeroDocumento; }
    public String getNombres() { return nombres; }
    public String getApellidos() { return apellidos; }
    public double getVentasTotales() { return ventasTotales; }
    
    public void registrarVenta(double monto) {
        this.ventasTotales += monto;
    }
}

/**
 * Clase principal encargada de la recolección, validación y reporte de los datos 
 * de ventas generados.
 */
public class Main {

    /**
     * Método principal de ejecución.
     * * @param args Argumentos de consola (no requeridos).
     */
    public static void main(String[] args) {
        try {
            System.out.println("Iniciando lectura y clasificación de datos...");

            // 1. Cargar datos maestros
            Map<String, Producto> mapaProductos = cargarDatos("productos.csv", linea -> {
                String[] datos = linea.split(";");
                return new Producto(datos[0], datos[1], Double.parseDouble(datos[2]));
            }, Producto::getId);

            Map<String, Vendedor> mapaVendedores = cargarDatos("vendedores.csv", linea -> {
                String[] datos = linea.split(";");
                return new Vendedor(datos[0], datos[1], datos[2], datos[3]);
            }, Vendedor::getNumeroDocumento);

            // 2. Procesar todos los archivos de ventas individuales
            Files.walk(Paths.get("."))
                 .filter(path -> path.getFileName().toString().startsWith("vendedor_") && path.getFileName().toString().endsWith(".csv"))
                 .forEach(path -> procesarArchivoVenta(path, mapaProductos, mapaVendedores));

            // 3. Generar reportes finales
            generarReportes(mapaVendedores, mapaProductos);

            System.out.println("¡Reportes 'reporte_vendedores.csv' y 'reporte_productos.csv' generados con éxito!");

        } catch (Exception e) {
            System.err.println("ERROR CRÍTICO: " + e.getMessage());
        }
    }

    /**
     * Método genérico para cargar información desde archivos CSV a mapas de memoria.
     * * @param <T> Tipo de entidad a cargar (Producto, Vendedor).
     * @param archivo Nombre del archivo a leer.
     * @param constructor Función que mapea una línea de texto a un objeto.
     * @param getKey Función que extrae la llave primaria del objeto para el Map.
     * @return Map con los datos procesados.
     * @throws IOException Si ocurre un problema leyendo el archivo.
     */
    private static <T> Map<String, T> cargarDatos(String archivo, java.util.function.Function<String, T> constructor, java.util.function.Function<T, String> getKey) throws IOException {
        if (!Files.exists(Paths.get(archivo))) {
            throw new FileNotFoundException("No se encontro el archivo maestro: " + archivo);
        }
        return Files.lines(Paths.get(archivo))
                    .map(constructor)
                    .collect(Collectors.toMap(getKey, item -> item));
    }

    /**
     * Lee un archivo de ventas individual, cruza la información y acumula los totales.
     * Incluye validaciones para ignorar datos inconsistentes.
     * * @param archivo Ruta del archivo del vendedor a procesar.
     * @param productos Mapa maestro de productos registrados.
     * @param vendedores Mapa maestro de vendedores registrados.
     */
    private static void procesarArchivoVenta(Path archivo, Map<String, Producto> productos, Map<String, Vendedor> vendedores) {
        try {
            List<String> lineas = Files.readAllLines(archivo);
            if (lineas.isEmpty()) return;

            String[] cabecera = lineas.get(0).split(";");
            if (cabecera.length < 2) return; // Archivo malformado
            
            String idVendedor = cabecera[1];
            Vendedor vendedor = vendedores.get(idVendedor);
            
            if (vendedor == null) {
                System.err.println("ADVERTENCIA: Archivo " + archivo.getFileName() + " pertenece a un vendedor no registrado (" + idVendedor + ").");
                return;
            }

            for (int i = 1; i < lineas.size(); i++) {
                String[] datos = lineas.get(i).split(";");
                if (datos.length < 2) continue; // Línea incompleta

                String idProducto = datos[0];
                int cantidad;
                
                try {
                    cantidad = Integer.parseInt(datos[1]);
                } catch (NumberFormatException e) {
                    continue; // Cantidad no numérica
                }

                Producto producto = productos.get(idProducto);

                // VALIDACIÓN EXTRA: Ignorar productos inexistentes o cantidades negativas
                if (producto == null) {
                    System.err.println("  -> Ignorando venta en " + archivo.getFileName() + ": ID de producto inexistente (" + idProducto + ").");
                    continue;
                }
                
                if (cantidad <= 0) {
                    System.err.println("  -> Ignorando venta en " + archivo.getFileName() + ": Cantidad no válida (" + cantidad + ").");
                    continue;
                }

                // Acumulación de ventas exitosa
                vendedor.registrarVenta(producto.getPrecio() * cantidad);
                producto.agregarVenta(cantidad);
            }
        } catch (Exception e) {
            System.err.println("ERROR procesando " + archivo.getFileName() + ": " + e.getMessage());
        }
    }

    /**
     * Clasifica los datos y genera los archivos CSV de reporte.
     * * @param mapaVendedores Información en memoria de los vendedores.
     * @param mapaProductos Información en memoria de los productos.
     * @throws IOException Si ocurre un error al escribir los reportes.
     */
    private static void generarReportes(Map<String, Vendedor> mapaVendedores, Map<String, Producto> mapaProductos) throws IOException {
        
        // Reporte 1: Vendedores ordenados descendentemente por dinero recaudado
        List<Vendedor> vendedoresOrdenados = mapaVendedores.values().stream()
                .sorted(Comparator.comparingDouble(Vendedor::getVentasTotales).reversed())
                .collect(Collectors.toList());

        try (PrintWriter writer = new PrintWriter("reporte_vendedores.csv", StandardCharsets.UTF_8.name())) {
            for (Vendedor v : vendedoresOrdenados) {
                writer.printf(Locale.US, "%s %s;%.2f\n", v.getNombres(), v.getApellidos(), v.getVentasTotales());
            }
        }

        // Reporte 2: Productos ordenados descendentemente por cantidad vendida
        List<Producto> productosOrdenados = mapaProductos.values().stream()
                .sorted(Comparator.comparingInt(Producto::getCantidadVendida).reversed())
                .collect(Collectors.toList());

        try (PrintWriter writer = new PrintWriter("reporte_productos.csv", StandardCharsets.UTF_8.name())) {
            for (Producto p : productosOrdenados) {
                // Se solicita imprimir solo Nombre y Precio
                writer.printf(Locale.US, "%s;%.2f\n", p.getNombre(), p.getPrecio());
            }
        }
    }
}
