// Clase que representa el comprobante de una venta exitosa.
// Estos objetos son los que apilaremos (push) en nuestro Stack para el historial.
public class Transaccion {

    // Identificador del pasajero, fundamental para buscar en la Pila y validar el límite de 5 boletos.
    private String cedula;

    // Almacena el destino para saber a qué furgoneta se le asignó esta venta.
    private String ruta;

    // Cantidad adquirida en este movimiento específico.
    private int cantidadBoletos;

    // Constructor que inicializa la transacción en el momento que se aprueba la compra.
    public Transaccion(String cedula, String ruta, int cantidadBoletos) {
        this.cedula = cedula;
        this.ruta = ruta;
        this.cantidadBoletos = cantidadBoletos;
    }

    // Métodos 'get' para poder leer los datos desde la clase principal 
    // cuando necesitemos recorrer la Pila para hacer validaciones matemáticas.

    public String getCedula() {
        return cedula;
    }

    public String getRuta() {
        return ruta;
    }

    public int getCantidadBoletos() {
        return cantidadBoletos;
    }
}