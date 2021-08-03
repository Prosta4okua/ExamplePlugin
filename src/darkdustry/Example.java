//Пакет, в котором лежит код
package darkdustry;

//Импорты пакетов из arc
import arc.util.*;
import arc.*;
import arc.graphics.*;

//Импорты пакетов из mindustry
import mindustry.mod.*;
import mindustry.Vars;
import mindustry.gen.*;
import mindustry.game.*;
import mindustry.net.*;
import mindustry.game.EventType.*;
import mindustry.content.*;
import mindustry.type.*;
import mindustry.entities.*;

//Импорты пакетов из джавы
import java.util.*;

public class Example extends Plugin {

    //Список, где хранятся проголосовавшие
    private final HashSet<String> votes = new HashSet<>();
    //Сколько процентов должно проголосовать, чтобы голосование было успешно
    private double ratio = 0.6;


    //Метод init() выполняется, когда сервер запускается
    @Override
    public void init() {
        
        //Делаем фильтр на действия игроков. 
        //В данном примере мы при каждом повороте блока отправляем игроку сообщение, в котором показано, какие координаты у этого блока.
        //Как видно здесь, у action есть несколько переменных внутри:
        //action.type, action.player, action.tile и т.д.
        //Полный список этого можно найти в коде самой mindustry.

        Vars.netServer.admins.addActionFilter(action -> {
            //Определяем тип действия
            if(action.type == Administration.ActionType.rotate) {
                //Проверяем, существует ли этот игрок
                if (action.player != null) {
                    //Отправляем игроку сообщение
                    action.player.sendMessage("Ты повернул блок на координатах [accent]" + action.tile.x + "[], [accent]" + action.tile.y);
                }
            }
            //Разрешаем игроку повернуть этот блок
            return true;
        });


        //Это один из ивентов в mindustry.
        //Он вызывается, когда загружается новая карта.

        Events.on(WorldLoadEvent.class, event -> {
            //Отправляем в чат сообщение, показывающее название карты
            Call.sendMessage("Вы играете на карте [cyan]" + Vars.state.map.name());
        });


        //Ивент нажатия на тайл.

        Events.on(TapEvent.class, event -> {
            //Объявляем переменную типа Player
            Player player = event.player;
            //Проверяем, является ли игрок админом
            if (player.admin()) {
                //Вызываем эффект "burning" в том месте, куда нажал игрок
                //Задаём этому эффекту поворот 100 градусов и цвет "#4169e1ff"
                Call.effect(Fx.burning, event.tile.x, event.tile.y, 100, Color.valueOf("#4169e1ff"));
            }
        });

        
        //Ивент подключения игрока к серверу. 
        //Вызывается при попытке подключения к нему, даже если игрок не зашёл.

        Events.on(PlayerConnect.class, event -> {
            Player player = event.player;
            //Проверяем, содержит ли никнейм игрока нужную фразу
            if(player.name().contains("Дарк")) {
                //Кикаем игрока по своей причине на 60000 секунд
                player.con.kick("Шизоидам на сервере не место", 60000);
            }
        });


        //По сути ивент, который происходит всегда.
        //Нужен для таймеров и т.д.

        Events.run(Trigger.update, () -> {
            //Находим всех юнитов типа "Мега"
            Groups.unit.each(u -> u.type() == UnitTypes.mega, m -> {
                //Задаём юниту значение "spawnedByCore" true
                //Это значит, что если его не контролирует игрок, то он моментально деспавнится
                m.spawnedByCore = true;
            });
        });


        //Ивент дисконнекта игрока
        //Если игрок был кикнут или забанен, он так же вызывается

        Events.on(PlayerLeave.class, event -> {
            //Проверяем, голосовал ли игрок за выдачу админки каждому
            if (!votes.contains(playerLeave.player.uuid())) return;
            //Убираем игрока из проголосовавших
            votes.remove(event.player.uuid());
            //Вычисляем, сколько сейчас голосов
            int cur = votes.size();
            //Вычисляем, сколько голосов надо для успешного завершения голосования
            int req = (int) Math.ceil(ratio * Groups.player.size());
            //Уведомляем всех о выходе игрока
            Call.sendMessage("[[scarlet]VOTEADMINS[white]]: " + player.name() + " вышел с сервера. Всего голосов: [cyan]" + cur + "[accent], необходимо голосов: [cyan]" +  req)
        });
    }

    //Этот метод содержит все слеш-команды для игроков
    @Override
    public void registerClientCommands(CommandHandler handler) {

        //Создаём простейшую команду, которая отправит в чат то, что ввел игрок
        //В скобках идёт сначала название, потом аргументы, потом описание команды
        //Если аргумент выделен через "<>", то он обязательный, а если через "[]", то необязательный
        //Если в аргументе есть "...", то в него попадет весь текст, который введёт игрок, а не только первое слово

        handler.<Player>register("say", "<Текст...>", "Сказать в чат анонимно.", (args, player) -> {
            Call.sendMessage("[lightgray]Анонимус[white]: " + args[0]);
        });


        //Как видно на этом примере, аргументов у команды может не быть совсем.
        //Создадим команду для голосования на выдачу админки всем игрокам на сервере.

        handler.<Player>register("voteadmins", "Голосовать за выдачу админки всем.", (args, player) -> {
            //Проверяем, голосовал ли игрок
            if (votes.contains(player.uuid())) {
                player.sendMessage("Ты уже проголосовал!");
                //return прерывает выполнение команды
                return;
            }
            //Добавляем игрока к проголосовавшим
            votes.add(player.uuid());
            //Получаем количество голосов на данный момент
            int cur = this.votes.size();
            //Получаем количество необходимых голосов
            int req = (int) Math.ceil(ratio * Groups.player.size());
            
            Call.sendMessage("[[scarlet]VOTEADMINS[white]]: " + player.name() + " [accent]проголосовал за выдачу админки каждому! Всего голосов: [cyan]" + cur + "[accent], необходимо голосов: [cyan]" +  req);

            //Проверяем, набраны ли голоса
            if (cur < req) return;
            
            //Голоса набраны, очищаем их
            this.votes.clear();
            //Выдаём админку каждому игроку (до перезахода на сервер)
            Groups.player.each(p -> p.admin = true);
            Call.sendMessage("[[scarlet]VOTEADMINS[white]]: [lime]голосование завершено, выдаю админку всем игрокам на сервере!");
        });
    }

    //Этот метод содержит все команды для консоли
    @Override
    public void registerServerCommands(CommandHandler handler) {

    }
}
