
/**
 * This class is a generic Linked list class It can have inputs in any formata
 * based on passed value of K, V
 * 
 * @author Anshu Anand
 *
 * @param <K>
 * @param <V>
 */
public class HashLinkedList<K, V> {

	public K key;
	public V val;
	public HashLinkedList<K, V> next;

	/**
	 * This is constructor for the linked-list
	 * @param s
	 * @param v
	 */
	public HashLinkedList(K s, V v) {

		key = s;
		val = v;
		next = null;

	}

	/**
	 * This method returns value of linked-list node
	 * @return
	 */
	public V get() {
		return val;
	}

	/**
	 * This method returns key of the linked list
	 */
	public K getKey() {
		return key;
	}

}