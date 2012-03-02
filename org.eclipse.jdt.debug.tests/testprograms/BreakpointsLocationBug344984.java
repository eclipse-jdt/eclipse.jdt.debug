public class BreakpointsLocationBug344984 {
    private final String fWorkingValues; // Breakpoint here 
    BreakpointsLocationBug344984() {
        fWorkingValues= null;
        System.out.println(fWorkingValues);
    }
}