;; Dope reigsters, technomancy.
(defvar refile-map (make-sparse-keymap))
(setq find-file-visit-truename t)

(defmacro defregister (key file)
  `(progn
     (set-register ,key (cons 'file ,file))
     (define-key refile-map
       (char-to-string ,key)
       (lambda (prefix)
         (interactive "p")
         (let ((org-refile-targets '(((,file) :maxlevel . 6)))
               (current-prefix-arg (or current-prefix-arg '(4))))
           (call-interactively 'org-refile))))))


(defregister ?A "~/sources/fjck/src/net/drilling")
(defregister ?a "~/sources/cmd")
(defregister ?b "~/.bashrc")
(defregister ?c "~/.config")
(defregister ?d "~/deps.edn")
(defregister ?D "~/sources/fjck/deps.edn")
(defregister ?f "~/sources/fjck")
(defregister ?i "~/.emacs.d/init.el")
(defregister ?k "~/.emacs.d/lisp/kbd.el")
(defregister ?p "~/package.json")
(defregister ?P "~/sources/fjck/src/net/drilling/pkgs")
(defregister ?r "~/.emacs.d/lisp/registers.el")
(defregister ?s "~/shadow-cljs.edn")
(defregister ?S "~/sources/fjck/shadow-cljs.edn")
(defregister ?t "~/.tmux.conf")
(defregister ?u "~/sources/fjck/src/net/drilling/ui")


