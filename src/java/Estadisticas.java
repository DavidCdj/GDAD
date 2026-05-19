//Entity Estadisticas:
//Estadisticas se compartira con todas las actividades, se guardara el numero de aciertos, errores, fecha y actividad realizada para luego ser guardada en un archivo de texto y mostrada en la pantalla de estadisticas
class Estadisticas {

    private int errores;
    private int aciertos;
    private String fecha;
    private String curp;
    private String tiempoActividad;
    private String actividad;
    
    public Estadisticas( String actividad, int aciertos, int errores, String curp, String tiempoActividad) {
        this.errores = errores;
        this.aciertos = aciertos;
        this.curp = curp;
        this.tiempoActividad = tiempoActividad;
        this.actividad = actividad;
        fecha = new java.text.SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date());

    }

    public Estadisticas() {
        this.errores = 0;
        this.aciertos = 0;
        this.curp = "";
        this.tiempoActividad = "";
        this.actividad = "";
        fecha = new java.text.SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date());

    }

    public int getErrores() {
        return errores;
    }

    public void setErrores(int errores) {
        this.errores = errores;
    }

    public int getAciertos() {
        return aciertos;
    }

    public void setAciertos(int aciertos) {
        this.aciertos = aciertos;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getActividad() {
        return actividad;
    }

    public void setActividad(String actividad) {
        this.actividad = actividad;
    }

    public String getCurp() {
        return curp;
    }

    public void setCurp(String curp) {
        this.curp = curp;
    }

    public String getTiempoActividad() {
        return tiempoActividad;
    }

    public void setTiempoActividad(String tiempoActividad) {
        this.tiempoActividad = tiempoActividad;
    }
}