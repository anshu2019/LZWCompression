
/**
 * This is hash map class
 * @author Anshu Anand
 *
 * @param <K>
 * @param <V>
 */
public class MyHashMap<K, V> {

	private HashLinkedList<K, V>[] bucketArray;

	private int totalSize; // total capacity of array list

	private int currentSize; // Current size of array list

	/**
	 * Constructor of the hashmap
	 * @param m
	 */
	public MyHashMap(int m) {
		bucketArray = new HashLinkedList[m];
		totalSize = m;
		currentSize = 0;

		// Create empty chains
		for (int i = 0; i < totalSize; i++)
			bucketArray[i] = null;
	}

	/**
	 * This method returns size of map
	 * @return
	 */
	public int size() {
		return currentSize;
	}

	/**
	 * this method checks emptiness of map
	 * @return
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * This method finds map index for the key 
	 * @param key
	 * @return
	 */
	private int getBucketIndex(K key) {
		//System.out.println("key: " + key);
		int hashCode = key.hashCode();
		if (hashCode < 0) {
			hashCode = -1 * hashCode;
		}
		
		int index = hashCode % totalSize;
		
		return index;
	}

	/**
	 * This method removes a key from the map
	 * @param key
	 * @return
	 */
	public V remove(K key) {
		// Apply hash function to find index for given key
		int bucketIndex = getBucketIndex(key);

		// Get head of chain
		HashLinkedList<K, V> head = bucketArray[bucketIndex];

		// Search for key in its chain
		HashLinkedList<K, V> prev = null;
		while (head != null) {
			// If Key found
			if (head.key.equals(key))
				break;

			// Else keep moving in chain
			prev = head;
			head = head.next;
		}

		// If key was not there
		if (head == null)
			return null;

		// Reduce size
		currentSize--;

		// Remove key
		if (prev != null)
			prev.next = head.next;
		else
			bucketArray[bucketIndex] = head.next;

		return head.val;
	}

	/**
	 * this method returns value of the node based on the key
	 * @param key
	 * @return
	 */
	public V get(K key) {
		// Find head of chain for given key
		//System.out.println("get key "+ key );
		int bucketIndex = getBucketIndex(key);
		HashLinkedList<K, V> head = bucketArray[bucketIndex];

		// Search key in chain
		while (head != null) {
			if (head.key.equals(key)) {				
				return head.val;
			}
			head = head.next;
		}

		// If key not found
		return null;
	}

	/**
	 * This method checks if map contains the key or not
	 * @param key
	 * @return
	 */
	public boolean contains(K key) {
		int bucketIndex = getBucketIndex(key);
		HashLinkedList<K, V> head = bucketArray[bucketIndex];

		// Search key in chain
		while (head != null) {
			if (head.key.equals(key)) {
				return true;
			}
			head = head.next;
		}

		// If key not found
		return false;
	}

	/**
	 * Thsi method adds new data to Map
	 * @param key
	 * @param value
	 */
	public void add(K key, V value) {
		// Find head of chain for given key
		int bucketIndex = getBucketIndex(key);
		HashLinkedList<K, V> head = bucketArray[bucketIndex];

		// Check if key is already present
		while (head != null) {
			if (head.key.equals(key)) {
				head.val = value;
				return;
			}
			head = head.next;
		}

		// Insert key in chain
		currentSize++;
		head = bucketArray[bucketIndex];
		HashLinkedList<K, V> newNode = new HashLinkedList<K, V>(key, value);
		newNode.next = head;
		bucketArray[bucketIndex] = newNode;

	}
}
