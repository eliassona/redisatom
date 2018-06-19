(ns redisatom.core
  (:use [clojure.pprint])
  (:import [clojure.lang IAtom IRef IDeref IObj IMeta]
           [redis.clients.jedis Jedis ScanParams]))


(defn value-of [jedis k]
  (let [d (.get jedis k)
        d (if d (read-string d) d)
        v (:value d)
        m (:meta d)]
    (if (instance? clojure.lang.IObj v)
      (with-meta v m)
      v)
    ))

(defmacro set-value [jedis k expr]
  `(let [v# ~expr
         res# (.set ~jedis ~k (pr-str {:meta (meta v#), :value v#}))]
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


(defn redis-keys
  "Lazy seq of the keys in redis"
  ([jedis]
    (redis-keys jedis "*"))
  ([jedis wildcard]
    (let [sp (ScanParams.)
          _ (.match sp wildcard)
          sr (.scan jedis "0" sp)]
        (redis-keys jedis (.getResult sr) (.getStringCursor sr) sp)))
  ([jedis the-keys next-cursor sp]
    (if (= next-cursor "0")
      the-keys
      (let [sr (.scan jedis next-cursor sp)]
        (lazy-seq (concat the-keys (redis-keys jedis (.getResult sr) (.getStringCursor sr) sp)))))))
    
   

  
(comment
(import 'redis.clients.jedis.HostAndPort)
(import 'redis.clients.jedis.JedisCluster)

(def host-set (into #{} (map (comp #(HostAndPort. "localhost" %) (partial + 7000)) (range 6))))
)