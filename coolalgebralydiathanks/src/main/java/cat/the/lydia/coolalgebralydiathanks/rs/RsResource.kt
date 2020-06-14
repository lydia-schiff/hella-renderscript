package cat.the.lydia.coolalgebralydiathanks.rs

/**
 * RenderScript components and Allocations are expensive and have manual destructors and classes
 * that use them tend to be stateful and mutable. It's good to be able reset one of these
 * RsResource classes to an initial state and to have a terminal destroyed state that releases all
 * memory.
 */
interface RsResource {
    /**
     * Reset to the initial state. Object must remain valid. If destroy was already called, this
     * should be a no-op. The call should be idempotent.
     */
    fun reset()

    /**
     * Release all memory and invalidate this object. If destroy() was already called, this should
     * be a no-op. Other methods should throw if the object is invalid. The call should be
     * idempotent.
     */
    fun destroy()
}
