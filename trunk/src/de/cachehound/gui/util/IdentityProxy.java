package de.cachehound.gui.util;

/**
 * Problem: ListFilter (und damit AndFilter und OrFilter) erben von List. Die
 * .equals()-Methode prueft also, ob die gleichen Objekte enthalten sind. Das
 * heisst: Wenn im JTreeView des FilterEditors ein Filter hinzugefuegt wird,
 * aendert sich der uebergeordnete Filter. Das bringt die JTreeView
 * durcheinander, und boese Exceptions fliegen.
 * 
 * Loesung: Der IdentityProxy implementiert ein .equals(), das auf Identitaet
 * prueft.
 */
public class IdentityProxy<T> {
	private T foo;
	
	public IdentityProxy(T foo) {
		this.foo = foo;
	}
	
	public T get() {
		return foo;
	}
	
	@SuppressWarnings("unchecked")
	public boolean equals(Object o) {
		return foo == ((IdentityProxy<T>)o).get();
	}
	
	public int hashCode() {
		return System.identityHashCode(foo);
	}
	
	public String toString() {
		return foo.toString();
	}

}
