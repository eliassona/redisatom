(ns redisatom.core
  (:use [clojure.pprint])
  (:import [clojure.lang IAtom IRef IDeref IObj IMeta]
           [redis.clients.jedis Jedis]))


(defn value-of [jedis k]
  (read-string (.get jedis k)))

(defmacro set-value [jedis k expr]
  `(let [v# ~expr
         res# (.set ~jedis ~k (pr-str v#))]
     (if (= res# "OK")
       v#
       res#) ;; throw exception here??
     ))

(deftype RedisAtom [jedis k]
    IAtom 
    (swap [_ f] (set-value jedis k (f (value-of jedis k))))
    (swap [_ f arg] (set-value jedis k (f (.get jedis k) arg)))

    (swap [_ f arg1 arg2] (set-value jedis k (f (value-of jedis k) arg1 arg2)))

    (swap [_ f x y args] (set-value jedis k (f (value-of jedis k) x y args)))

    (compareAndSet [_ oldv newv] (println oldv))
    (reset [_ obj]  (set-value jedis k obj))
    
IRef
  (setValidator [_ validator]
    )
  (getValidator [_]
    )
  (addWatch [this watch-key watch-fn]
    
    this)
  (removeWatch [this watch-key]
    
    this)
  (getWatches [_]
    )
  IDeref
  (deref [_]
    (value-of jedis k))
    
  IObj
  (withMeta [_ meta-map]
    )
  IMeta
  (meta [_]
    ))


(defn redis-atom [jedis]
  (fn [k] (->RedisAtom jedis k)))
  
    