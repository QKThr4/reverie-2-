#!/usr/bin/env python3
"""Writes en_us.json and pt_br.json. Every text of the story lives here."""
import json, os

OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources", "assets",
                   "liminalreverie", "lang")
T = []  # (key, en, pt)
def add(k, en, pt): T.append((k, en, pt))

# ---- game objects
add("itemGroup.liminalreverie", "Liminal Reverie", "Liminal Reverie")
add("key.liminalreverie.journal", "Open Dream Journal", "Abrir Diário dos Sonhos")
add("key.categories.liminalreverie", "Liminal Reverie", "Liminal Reverie")
for k, en, pt in [
    ("block.liminalreverie.wallpaper", "Beige Wallpaper", "Papel de Parede Bege"),
    ("block.liminalreverie.liminal_carpet", "Liminal Carpet", "Carpete Liminal"),
    ("block.liminalreverie.fluorescent_light", "Fluorescent Light", "Luz Fluorescente"),
    ("block.liminalreverie.pool_tile", "Pool Tile", "Azulejo de Piscina"),
    ("block.liminalreverie.mall_tile", "Mall Tile", "Piso de Shopping"),
    ("block.liminalreverie.memory_fragment", "Memory Fragment", "Fragmento de Memória"),
    ("block.liminalreverie.symbol_mark", "Symbol Mark", "Marca de Símbolo"),
    ("item.liminalreverie.journal", "Dream Journal", "Diário dos Sonhos"),
    ("item.liminalreverie.brass_key", "Brass Key", "Chave de Latão"),
    ("item.liminalreverie.faded_ticket", "Faded Ticket", "Bilhete Desbotado"),
    ("entity.liminalreverie.caretaker", "The Caretaker", "O Zelador"),
    ("entity.liminalreverie.child", "The Child", "A Criança"),
    ("entity.liminalreverie.wanderer", "The Wanderer", "O Andarilho"),
    ("entity.liminalreverie.sleeper", "The Sleeper", "O Adormecido"),
]: add(k, en, pt)

# ---- chapters, areas, places
for i, (en, pt) in enumerate([("Things That Don't Belong", "Coisas Que Não Pertencem"), ("The Places Between", "Os Lugares Entre"),
                              ("The People Who Are Still Here", "As Pessoas Que Ainda Estão Aqui"),
                              ("The Last Room", "O Último Quarto")], 1):
    add("lr.chapter.%d" % i, en, pt)

AREAS = [
    ("lobby", "The Lobby", "O Saguão", "A hotel lobby too large for any hotel. The hum is steady. Someone keeps it clean.",
     "Um saguão grande demais para qualquer hotel. O zumbido é constante. Alguém mantém tudo limpo."),
    ("school", "The Empty School", "A Escola Vazia", "Twelve classrooms, one long corridor, and a sun that is always about to set.",
     "Doze salas, um corredor comprido e um sol que está sempre quase se pondo."),
    ("pool", "The Blue Pool", "A Piscina Azul", "Enormous, tiled, quiet. The water remembers more than the walls do.",
     "Enorme, azulejada, silenciosa. A água lembra mais do que as paredes."),
    ("hotel", "The Hotel", "O Hotel", "Hundreds of doors. Most are only rooms. A few are something else.",
     "Centenas de portas. A maioria é só um quarto. Algumas são outra coisa."),
    ("mall", "The Endless Mall", "O Shopping Sem Fim", "Music from nowhere. Stores that sell afternoons.",
     "Música de lugar nenhum. Lojas que vendem tardes."),
    ("fields", "Sunset Fields", "Campos do Pôr do Sol", "Grass, wire and an orange sky that never gives up. The most peaceful place so far.",
     "Grama, fios e um céu laranja que nunca desiste. O lugar mais tranquilo até agora."),
    ("lounge", "The Lounge", "A Sala de Estar", "Warm lamps and soft chairs. Nothing here has ever gone wrong.",
     "Lâmpadas quentes e poltronas macias. Nada aqui jamais deu errado."),
    ("rooftop", "The Rooftop", "O Terraço", "A city that looks finished, like a drawing. No sound at all.",
     "Uma cidade que parece pronta, como um desenho. Nenhum som."),
    ("train", "The Empty Train", "O Trem Vazio", "It never moves. The fields go by anyway.",
     "Nunca anda. Os campos passam do mesmo jeito."),
    ("bedroom", "The Bedroom", "O Quarto", "Small. Familiar. It seems to change when you aren't looking.",
     "Pequeno. Familiar. Parece mudar quando você não está olhando."),
    ("last_room", "The Last Room", "O Último Quarto", "An ordinary bedroom. One window, one bed, one door.",
     "Um quarto comum. Uma janela, uma cama, uma porta."),
]
for aid, en, pt, den, dpt in AREAS:
    add("lr.area." + aid, en, pt)
    add("lr.place." + aid, den, dpt)

# ---- memories
MEM = [
 ("The first thing I remember is a hum. Warm, steady, beige.", "A primeira coisa de que me lembro é um zumbido. Quente, constante, bege."),
 ("Someone had watered the plants. I never saw who.", "Alguém tinha regado as plantas. Nunca vi quem."),
 ("Room nine had the best light. We sat by the window and pretended the clock was slow.", "A sala nove tinha a melhor luz. Sentávamos na janela e fingíamos que o relógio estava atrasado."),
 ("Chalk dust looks like snow when the sun comes in at four o'clock.", "O pó de giz parece neve quando o sol entra às quatro da tarde."),
 ("I kept a key in my shoe for a whole year. I never learned what it opened.", "Guardei uma chave no sapato por um ano inteiro. Nunca descobri o que ela abria."),
 ("The pool was loud once. Everyone shouting numbers, everyone counting to ten.", "A piscina já foi barulhenta. Todo mundo gritando números, todo mundo contando até dez."),
 ("I could hold my breath for exactly one song. Then I came up and the world had moved on.", "Eu segurava a respiração por exatamente uma música. Depois subia e o mundo já tinha seguido em frente."),
 ("Under the water the noise stops. I think that is why I liked it.", "Debaixo da água o barulho para. Acho que por isso eu gostava."),
 ("Room 204. The door never locked, and I never wanted it to.", "Quarto 204. A porta nunca trancava, e eu nunca quis que trancasse."),
 ("At hotels we were always arriving or leaving. Never staying.", "Em hotéis estávamos sempre chegando ou indo embora. Nunca ficando."),
 ("The elevator took longer on the way down. I counted. Nobody believed me.", "O elevador demorava mais para descer. Eu contei. Ninguém acreditou."),
 ("In the mall the music came from a place you could never find.", "No shopping a música vinha de um lugar que ninguém achava."),
 ("I won every game I played alone. After a while it stopped counting.", "Ganhei todos os jogos que joguei sozinho. Depois de um tempo, isso parou de contar."),
 ("A paper ticket, folded in four, went through the wash. I kept it anyway.", "Um bilhete de papel, dobrado em quatro, passou pela máquina de lavar. Guardei mesmo assim."),
 ("The road went on past the last pole, and I decided that was the edge of the world.", "A estrada seguia além do último poste, e decidi que ali era a beira do mundo."),
 ("We swung until the chains sang. Then someone called us in for dinner.", "Nos balançamos até as correntes cantarem. Aí alguém nos chamou para jantar."),
 ("I waited for a bus I knew wasn't coming. It felt like doing something.", "Esperei por um ônibus que eu sabia que não viria. Parecia estar fazendo alguma coisa."),
 ("Home smelled like warm bread and old paper. I can't remember the street.", "Em casa cheirava a pão quente e papel velho. Não lembro da rua."),
 ("From the roof the city had no sound. It looked finished, like a drawing.", "Do terraço a cidade não fazia som. Parecia pronta, como um desenho."),
 ("The train never moved, but the fields went by anyway.", "O trem nunca andou, mas os campos passaram do mesmo jeito."),
 ("My room had one window, and it always faced the evening.", "Meu quarto tinha uma janela, e ela sempre dava para o fim de tarde."),
 ("I fell asleep on purpose, once, to see where the rooms went.", "Uma vez peguei no sono de propósito, só para ver para onde os quartos iam."),
 ("Some things only make sense from underneath.", "Algumas coisas só fazem sentido vistas de baixo."),
 ("There is a room behind the books. I put my worries there.", "Existe um quarto atrás dos livros. Guardei minhas preocupações lá."),
 ("Room zero. I checked. It exists. I am not sure it exists for anyone else.", "Quarto zero. Eu verifiquei. Existe. Não sei se existe para mais alguém."),
 ("Behind the vents, the mall hums the same song as the lobby.", "Atrás dos dutos, o shopping cantarola a mesma música do saguão."),
 ("I buried a lunchbox under the biggest tree. Look for what grew.", "Enterrei uma lancheira debaixo da árvore maior. Procure o que cresceu."),
 ("The lobby was never empty. I only ever came at the hour when everyone had gone.", "O saguão nunca esteve vazio. Eu só chegava na hora em que todos já tinham ido."),
 ("At the edge of the roof I stopped being afraid of the dark. It was only evening.", "Na beira do terraço parei de ter medo do escuro. Era só o fim de tarde."),
 ("I hid a note under my bed: 'Don't forget the way back.'", "Escondi um bilhete embaixo da cama: 'Não esqueça o caminho de volta.'"),
 ("Warm lamps. Quiet voices. I would like to stay a little longer.", "Lâmpadas quentes. Vozes baixas. Queria ficar um pouco mais."),
 ("Whoever you are: thank you for walking through all of it with me.", "Quem quer que você seja: obrigado por caminhar por tudo isso comigo."),
]
assert len(MEM) == 32
for i, (en, pt) in enumerate(MEM, 1):
    add("lr.memory.%d" % i, en, pt)

# ---- npcs & dialogue
NPC = {
 "caretaker": ("The Caretaker", "O Zelador", "Keeps the rooms as they were left.", "Mantém os quartos como foram deixados."),
 "child": ("The Child", "A Criança", "Draws places before you reach them.", "Desenha os lugares antes de você chegar."),
 "wanderer": ("The Wanderer", "O Andarilho", "Has walked here for a very long time.", "Anda por aqui há muito tempo."),
 "sleeper": ("The Sleeper", "O Adormecido", "Resting in a perfectly ordinary bedroom.", "Descansando num quarto perfeitamente comum."),
}
for k, (en, pt, den, dpt) in NPC.items():
    add("lr.npc." + k, en, pt)
    add("lr.npc.desc." + k, den, dpt)

DLG = {
 "caretaker": [
  ("Ah, there you are. Please, don't mind the hum. It means the lights are still on.", "Ah, aí está você. Não ligue para o zumbido. Significa que as luzes ainda estão acesas."),
  ("Every room is kept as it was left. I only dust the edges of things.", "Cada quarto é mantido como foi deixado. Eu só tiro o pó das beiradas das coisas."),
  ("You're not from here. That's alright. Most visitors aren't, at first.", "Você não é daqui. Tudo bem. A maioria dos visitantes não é, no começo."),
  ("I won't tell you how to leave. Not because I mustn't. Because you'd stop looking.", "Não vou dizer como sair. Não porque eu não possa. Porque você pararia de procurar."),
  ("Someone built all this out of afternoons. I only keep the doors on their hinges.", "Alguém construiu tudo isso com tardes. Eu só mantenho as portas nas dobradiças."),
  ("You've seen nearly every room. Have you noticed they all have the same window?", "Você viu quase todos os quartos. Reparou que todos têm a mesma janela?")],
 "child": [
  ("I drew this for you. You haven't been there yet, but you will.", "Desenhei isto para você. Você ainda não foi lá, mas vai."),
  ("Do you hear that? The classroom is trying to ring.", "Está ouvindo? A sala de aula está tentando tocar."),
  ("Pools are just sky that fell down and decided to stay.", "Piscinas são só céu que caiu e decidiu ficar."),
  ("I drew a hotel with too many doors. You'll know the right one.", "Desenhei um hotel com portas demais. Você vai saber qual é a certa."),
  ("The mall has no inside. Just more outside, with a roof.", "O shopping não tem dentro. Só mais fora, com teto."),
  ("I'm always the same age here. It's nice. Is it nice where you are?", "Aqui eu tenho sempre a mesma idade. É bom. Onde você está também é?")],
 "wanderer": [
  ("Another one. Sit, if the couches still hold you. I've lost count of how long I've been walking.", "Mais um. Sente, se os sofás ainda aguentarem você. Perdi a conta de quanto tempo ando."),
  ("The rooms don't repeat. They rhyme. You'll hear it after a while.", "Os quartos não se repetem. Eles rimam. Você vai ouvir depois de um tempo."),
  ("I left a note at every door I understood. Most of them I wrote to myself.", "Deixei um bilhete em cada porta que entendi. A maioria eu escrevi para mim mesmo."),
  ("At first I looked for the exit. Then I started looking for whoever built this.", "No começo procurei a saída. Depois passei a procurar quem construiu isto."),
  ("Every place here is somewhere I stopped paying attention to. That's what makes it so large.", "Cada lugar aqui é um lugar a que parei de prestar atenção. É isso que o faz tão grande."),
  ("The Caretaker and the Child I know well. The one who sleeps, I only see from the door.", "O Zelador e a Criança eu conheço bem. Quem dorme, só vejo da porta."),
  ("If you reach the last room, do me a favour. Look at the clock. Tell me if it's still on time.", "Se chegar ao último quarto, faça-me um favor. Olhe o relógio. Diga se ainda está na hora.")],
 "sleeper": [
  ("...mm. Is it evening already?", "...mm. Já é fim de tarde?"),
  ("I'm not asleep. I'm just resting where the memories are warm.", "Não estou dormindo. Só descansando onde as memórias são quentes."),
  ("You carried every one of them back to me. I think I remember your footsteps.", "Você trouxe cada uma de volta para mim. Acho que lembro dos seus passos."),
  ("Whatever you choose, nothing here will be lost. It was always going to be kept.", "O que quer que escolha, nada aqui se perderá. Sempre ia ser guardado.")],
}
IDLE = {
 "caretaker": [("Mind the carpet. It remembers footsteps.", "Cuidado com o carpete. Ele lembra dos passos."),
               ("Everything is exactly where it should be. Even you.", "Tudo está exatamente onde deveria. Até você."),
               ("You were always here. I just hadn't introduced myself properly.", "Você sempre esteve aqui. Eu só não tinha me apresentado direito.")],
 "child": [("Do you want to see my drawing again?", "Quer ver meu desenho de novo?"),
           ("I'll wait here. I'm good at waiting.", "Eu espero aqui. Sou boa em esperar."),
           ("You came back! I knew you would.", "Você voltou! Eu sabia que voltaria.")],
 "wanderer": [("Keep walking. The good rooms are always one door further.", "Continue andando. Os melhores quartos ficam sempre uma porta adiante."),
              ("I'm fine here. I think I'm fine here.", "Estou bem aqui. Acho que estou bem aqui."),
              ("Funny. It stopped feeling like a maze the day you stopped counting.", "Engraçado. Deixou de parecer um labirinto no dia em que você parou de contar.")],
 "sleeper": [("(They smile in their sleep.)", "(Sorriem enquanto dormem.)"),
             ("(The breathing is slow and even.)", "(A respiração é lenta e regular.)"),
             ("(They look peaceful.)", "(Parecem em paz.)")],
}
for k, lines in DLG.items():
    for i, (en, pt) in enumerate(lines, 1):
        add("lr.dlg.%s.%d" % (k, i), en, pt)
    (e1, p1), (e2, p2), (e3, p3) = IDLE[k]
    add("lr.dlg.%s.idle1" % k, e1, p1)
    add("lr.dlg.%s.idle2" % k, e2, p2)
    add("lr.dlg.%s.stay" % k, e3, p3)

# ---- puzzles
PUZ = [
 ("lobby_symbols", "The Directory", "O Diretório", "The directory lights up. Somewhere, a door was waiting for this.", "O diretório se acende. Em algum lugar, uma porta esperava por isso."),
 ("school_classroom", "School Classroom Puzzle", "Puzzle da Sala de Aula", "This was the room. The window faces the evening.", "Era esta a sala. A janela dá para o fim de tarde."),
 ("school_lockers", "Locker Combination", "Combinação dos Armários", "A locker opens. Inside: a brass key.", "Um armário se abre. Dentro: uma chave de latão."),
 ("pool_sequence", "Pool Sequence", "Sequência da Piscina", "The tiles hum back at you.", "Os azulejos zumbem de volta para você."),
 ("pool_drain", "The Drain", "O Ralo", "The drain opens onto a quiet passage below.", "O ralo se abre para uma passagem silenciosa lá embaixo."),
 ("hotel_elevator", "Hotel Elevator Puzzle", "Puzzle do Elevador do Hotel", "The elevator hums. It likes that order.", "O elevador zumbe. Ele gosta dessa ordem."),
 ("hotel_loop", "The Hallway That Loops", "O Corredor Que Se Repete", "The corridor finally lets you go.", "O corredor finalmente deixa você ir."),
 ("mall_arcade", "Mall Arcade Puzzle", "Puzzle do Fliperama", "All five screens flicker, then agree.", "As cinco telas piscam e depois concordam."),
 ("mall_shop", "The Closed Shop", "A Loja Fechada", "The shop opens. A ticket was waiting on the counter.", "A loja se abre. Um bilhete esperava no balcão."),
 ("train_ticket", "The Ticket", "O Bilhete", "The slot accepts the ticket. The doors begin to close.", "A fenda aceita o bilhete. As portas começam a fechar."),
 ("fields_poles", "The Humming Poles", "Os Postes Que Zumbem", "The wires sing one clear note.", "Os fios cantam uma única nota clara."),
 ("last_clock", "The Clock", "O Relógio", "The clock is on time. It was always on time.", "O relógio está na hora. Sempre esteve."),
]
for pid, en, pt, den, dpt in PUZ:
    add("lr.puzzle." + pid, en, pt)
    add("lr.puzzle.%s.done" % pid, den, dpt)

# ---- notes & messages
NOTES = [
 ("lobby_desk", "A guest book. The last entry is today's date, in handwriting you almost recognise.", "Um livro de visitas. A última entrada é a data de hoje, numa letra que você quase reconhece."),
 ("library", "A reading slip: 'The lockers open for those who count from the entrance: the third, then the eighth, then the fifth.'", "Uma ficha de leitura: 'Os armários abrem para quem conta a partir da entrada: o terceiro, depois o oitavo, depois o quinto.'"),
 ("class_wrong.1", "The chairs are all turned toward the window. Nothing here.", "As cadeiras estão todas viradas para a janela. Nada aqui."),
 ("class_wrong.2", "A faded timetable. Every period says 'Afternoon'.", "Uma grade de horários desbotada. Todo período diz 'Tarde'."),
 ("class_wrong.3", "Someone drew a sun on the board and never wiped it.", "Alguém desenhou um sol no quadro e nunca apagou."),
 ("pool_poster", "A faded poster: 'TOUCH THE TILES IN ORDER: blue, yellow, red, green.'", "Um cartaz desbotado: 'TOQUE NOS AZULEJOS NA ORDEM: azul, amarelo, vermelho, verde.'"),
 ("fields_start", "A signpost: 'Follow the road. The wires hum if you listen. The bus stop has a timetable. The house at the end of the field is open.'", "Uma placa: 'Siga a estrada. Os fios zumbem se você escutar. O ponto de ônibus tem uma tabela. A casa no fim do campo está aberta.'"),
 ("house", "A table set for dinner. Two plates. One chair is still warm. A note: 'The way back is the way you came.'", "Uma mesa posta para o jantar. Dois pratos. Uma cadeira ainda está quente. Um bilhete: 'O caminho de volta é o caminho por onde você veio.'"),
 ("hotel_elevator", "A guest note: 'I always press three, then one, then four. The elevator likes it that way.'", "Um bilhete de hóspede: 'Eu sempre aperto três, depois um, depois quatro. O elevador gosta assim.'"),
 ("hotel_loop", "A note: 'If the corridor repeats, trust your right hand, then your left, then your right.'", "Um bilhete: 'Se o corredor se repetir, confie na mão direita, depois na esquerda, depois na direita.'"),
 ("arcade", "A high-score board: 'Second, then fourth, then first, then third.' No initials.", "Um placar: 'Segundo, depois quarto, depois primeiro, depois terceiro.' Sem iniciais."),
 ("bus_stop", "A timetable scratched with pen: 'Start with the middle pole, then the first, then the last, then the second. Listen.'", "Uma tabela riscada a caneta: 'Comece pelo poste do meio, depois o primeiro, depois o último, depois o segundo. Escute.'"),
 ("lounge_books", "Every spine is blank, except one: 'Don't forget the way back.'", "Todas as lombadas estão em branco, exceto uma: 'Não esqueça o caminho de volta.'"),
 ("bedroom_desk", "A page of homework. The answer to every question is 'home'. In the margin: 'The clock: second, fourth, first, third.'", "Uma folha de lição de casa. A resposta de todas as perguntas é 'casa'. Na margem: 'O relógio: segundo, quarto, primeiro, terceiro.'"),
 ("last_remember_locked", "The desk drawer is stuck. You haven't remembered enough yet.", "A gaveta da escrivaninha emperrou. Você ainda não lembrou o bastante."),
 ("last_window_locked", "The window looks painted on. Something about it is still missing.", "A janela parece pintada. Ainda falta alguma coisa nela."),
]
for k, en, pt in NOTES: add("lr.note." + k, en, pt)

LOCKED = {
 "lobby": ("The door doesn't open.", "A porta não abre."),
 "school": ("The door stays shut. Perhaps someone here can explain.", "A porta continua fechada. Talvez alguém aqui possa explicar."),
 "pool": ("The pool door is locked. The school isn't finished with you.", "A porta da piscina está trancada. A escola ainda não terminou com você."),
 "hotel": ("The way to the hotel is closed. There is still something under the pool.", "O caminho para o hotel está fechado. Ainda há algo debaixo da piscina."),
 "mall": ("The elevator doesn't move. The hotel isn't finished with you.", "O elevador não se move. O hotel ainda não terminou com você."),
 "fields": ("No train has arrived yet.", "Nenhum trem chegou ainda."),
 "lounge": ("Not yet. Someone should speak to you first.", "Ainda não. Alguém deveria falar com você primeiro."),
 "rooftop": ("The stairs lead nowhere until the hotel is understood.", "A escada não leva a lugar nenhum até o hotel ser compreendido."),
 "train": ("The platform is closed.", "A plataforma está fechada."),
 "bedroom": ("This door is only a wall, for now.", "Esta porta é só uma parede, por enquanto."),
 "last_room": ("There is no such floor.", "Não existe esse andar."),
}
for k, (en, pt) in LOCKED.items(): add("lr.msg.locked." + k, en, pt)
for k, en, pt in [
 ("forming", "The dream is taking shape...", "O sonho está tomando forma..."),
 ("solved", "Already solved.", "Já resolvido."),
 ("reset", "Nothing happens. The order was wrong.", "Nada acontece. A ordem estava errada."),
 ("need_key", "The door is locked. You need a brass key.", "A porta está trancada. Você precisa de uma chave de latão."),
 ("need_ticket", "The slot wants a ticket.", "A fenda quer um bilhete."),
 ("panel", "The directory shows three symbols you haven't found yet.", "O diretório mostra três símbolos que você ainda não encontrou."),
 ("loop", "The corridor folds back on itself.", "O corredor se dobra sobre si mesmo."),
 ("ride.1", "The doors close softly.", "As portas se fecham suavemente."),
 ("ride.2", "Fields slide past the window, slower than they should.", "Campos deslizam pela janela, mais devagar do que deveriam."),
 ("ride.3", "The light is orange. It will be orange for a long time.", "A luz é laranja. Vai ser laranja por muito tempo."),
]: add("lr.msg." + k, en, pt)

for k, en, pt in [
 ("memory", "Memory Fragment discovered", "Fragmento de Memória descoberto"),
 ("puzzle", "Puzzle completed — %s", "Puzzle concluído — %s"),
 ("area", "New location discovered", "Novo lugar descoberto"),
 ("chapter", "Journal updated: a new chapter", "Diário atualizado: um novo capítulo"),
 ("secret", "A secret was found", "Um segredo foi encontrado"),
 ("symbol", "Symbol discovered", "Símbolo descoberto"),
 ("ending", "Journal updated", "Diário atualizado"),
]: add("lr.notify." + k, en, pt)

END = {
 "wake": [("The room settles around you.", "O quarto se assenta ao seu redor."), ("The window is bright, and ordinary.", "A janela está clara, e comum."), ("Morning.", "Manhã.")],
 "stay": [("The hum softens.", "O zumbido se suaviza."), ("Nobody closes the doors.", "Ninguém fecha as portas."), ("You stay, and the evening stays with you.", "Você fica, e o fim de tarde fica com você.")],
 "remember": [("The lobby was the first room you ever walked out of.", "O saguão foi o primeiro quarto de onde você saiu."),
              ("The school, the pool, the mall: small afternoons you kept.", "A escola, a piscina, o shopping: pequenas tardes que você guardou."),
              ("The Caretaker is your habit of keeping. The Child, who you were. The Wanderer, who you feared becoming.", "O Zelador é seu hábito de guardar. A Criança, quem você foi. O Andarilho, quem você temia se tornar."),
              ("And the Sleeper is simply you, resting until you were ready to remember.", "E o Adormecido é apenas você, descansando até estar pronto para lembrar.")],
 "window": [("You open the window.", "Você abre a janela."), ("The evening is not outside. It is where you were standing all along.", "O fim de tarde não está lá fora. É onde você sempre esteve."), ("You were never trapped. You were visiting.", "Você nunca esteve preso. Estava visitando.")],
}
for k, lines in END.items():
    for i, (en, pt) in enumerate(lines, 1): add("lr.end.%s.%d" % (k, i), en, pt)

# ---- UI
for k, en, pt in [
 ("title", "Dream Journal", "Diário dos Sonhos"),
 ("tab.progress", "Progress", "Progresso"), ("tab.memories", "Memories", "Memórias"),
 ("tab.characters", "Characters", "Personagens"), ("tab.places", "Places", "Lugares"), ("tab.symbols", "Symbols", "Símbolos"),
 ("sec.chapters", "CHAPTER PROGRESS", "PROGRESSO DOS CAPÍTULOS"), ("sec.areas", "AREA PROGRESS", "PROGRESSO DAS ÁREAS"),
 ("sec.puzzles", "PUZZLE PROGRESS", "PROGRESSO DOS PUZZLES"), ("sec.memories", "MEMORY PROGRESS", "PROGRESSO DAS MEMÓRIAS"),
 ("sec.characters", "CHARACTER PROGRESS", "PROGRESSO DOS PERSONAGENS"), ("sec.secrets", "SECRETS", "SEGREDOS"),
 ("chapter", "Chapter %s — %s", "Capítulo %s — %s"), ("dash", "%s — %s", "%s — %s"),
 ("state.completed", "COMPLETED", "CONCLUÍDO"), ("state.progress", "IN PROGRESS", "EM ANDAMENTO"), ("state.locked", "LOCKED", "BLOQUEADO"),
 ("area.0", "Unknown", "Desconhecido"), ("area.1", "Locked", "Bloqueado"), ("area.2", "Not Discovered", "Não Descoberto"),
 ("area.3", "Discovered", "Descoberto"), ("area.4", "In Progress", "Em Andamento"), ("area.5", "Completed", "Concluído"),
 ("puzzles_done", "Puzzles Completed: %s / %s", "Puzzles Concluídos: %s / %s"), ("puzzle_line", "%s %s", "%s %s"),
 ("memories_count", "Memory Fragments: %s / %s", "Fragmentos de Memória: %s / %s"),
 ("found", "[x]", "[x]"),
 ("character_line", "%s — %s / %s", "%s — %s / %s"),
 ("secret_rooms", "Secret Rooms: %s / %s", "Salas Secretas: %s / %s"), ("hidden_events", "Hidden Events: %s / %s", "Eventos Ocultos: %s / %s"),
 ("symbols_count", "Symbols: %s / %s", "Símbolos: %s / %s"), ("optional_memories", "Optional Memories: %s / %s", "Memórias Opcionais: %s / %s"),
 ("soon", "More areas, secrets and story: COMING SOON", "Mais áreas, segredos e história: EM BREVE"),
 ("endings_count", "Endings: %s / %s", "Finais: %s / %s"), ("overall", "REVERIE — %s%%", "REVERIE — %s%%"),
]: add("lr.ui." + k, en, pt)

for k, en, pt in [
 ("welcome", "[Liminal Reverie] The mod is active. Play normally, and keep your eyes open.", "[Liminal Reverie] O mod está ativo. Jogue normalmente e fique de olhos abertos."),
 ("journal_locked", "The pages are blank. Maybe later.", "As páginas estão em branco. Talvez mais tarde."),
]: add("lr.msg." + k, en, pt)
for k, en, pt in [("wake", "Wake Up", "Acordar"), ("stay", "Stay", "Ficar"), ("remember", "Remember", "Lembrar"), ("window", "The Window", "A Janela")]:
    add("lr.sign." + k, en, pt)
for k, en, pt in [
 ("wake", "ENDING 1 OF 4: WAKE UP\n\nYou return to the ordinary world. The Reverie closes behind you. One small thing is different.",
          "FINAL 1 DE 4: ACORDAR\n\nVocê volta ao mundo comum. O Reverie se fecha atrás de você. Uma coisa pequena está diferente."),
 ("stay", "ENDING 2 OF 4: STAY\n\nYou stay. Every door is open now, and you can keep exploring.",
          "FINAL 2 DE 4: FICAR\n\nVocê fica. Todas as portas estão abertas agora, e você pode continuar explorando."),
 ("remember", "ENDING 3 OF 4: REMEMBER\n\nYou return to the Last Room. The doors stay open, and you can keep exploring.",
          "FINAL 3 DE 4: LEMBRAR\n\nVocê volta ao Último Quarto. As portas continuam abertas, e você pode continuar explorando."),
 ("window", "SECRET ENDING 4 OF 4: THE WINDOW\n\nYou stay. Every door is open now, and you can keep exploring.",
          "FINAL SECRETO 4 DE 4: A JANELA\n\nVocê fica. Todas as portas estão abertas agora, e você pode continuar explorando."),
]: add("lr.end.final." + k, en, pt)

en_d, pt_d = {}, {}
for k, en, pt in T:
    assert k not in en_d, "duplicate key " + k
    en_d[k], pt_d[k] = en, pt
os.makedirs(OUT, exist_ok=True)
for name, d in (("en_us", en_d), ("pt_br", pt_d)):
    with open(os.path.join(OUT, name + ".json"), "w", encoding="utf-8") as f:
        json.dump(d, f, indent=2, ensure_ascii=False)
print(len(T), "keys")
