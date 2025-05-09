package com.pl.premier_zone.player;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;

@Component
public class PlayerService {

    private final PlayerRepository playerRepository;

    @Autowired
    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public List<Player> getPlayers() {
        return playerRepository.findAll();
    }

    public Player getPlayerById(Long id) {
        return playerRepository.findById(id).orElse(null);
    }

    public List<Player> getPlayersFromTeam(String teamName){
        return playerRepository.findAll().stream()
                .filter(player -> teamName.equalsIgnoreCase(player.getTeam()))
                .collect(Collectors.toList()) ;
    }

    public List<Player> getPlayersByName(String searchText){
        return playerRepository.findAll().stream()
                .filter(player -> player.getName().toLowerCase().contains(searchText.toLowerCase()))
                .collect(Collectors.toList()) ;
    }

    public List<Player> getPlayerByPos(String searchText){
        return playerRepository.findAll().stream()
                .filter(player -> player.getPos().toLowerCase().contains(searchText.toLowerCase()))
                .collect(Collectors.toList()) ;
    }

    public List<Player> getPlayerByNation(String searchText){
        return playerRepository.findAll().stream()
                .filter(player -> player.getNation().toLowerCase().contains(searchText.toLowerCase()))
                .collect(Collectors.toList()) ;
    }

    public List<Player>  getPlayersByTeamAndPosition(String teamName, String position) {
        return playerRepository.findAll().stream()
                .filter(player -> teamName.equalsIgnoreCase(player.getTeam()) && position.equalsIgnoreCase(player.getPos()))
                .collect(Collectors.toList());
    }

    public Player addPlayer(Player player){
        return playerRepository.save(player);
    }

    public Player updatePlayer(Long id, Player player){
        Optional<Player> existingPlayer = playerRepository.findById(id);

        if (existingPlayer.isPresent()) {
            Player updatedPlayer = existingPlayer.get();

            updatedPlayer.setName(player.getName());
            updatedPlayer.setNation(player.getNation());
            updatedPlayer.setPos(player.getPos());
            updatedPlayer.setAge(player.getAge());
            updatedPlayer.setMp(player.getMp());
            updatedPlayer.setStarts(player.getStarts());
            updatedPlayer.setMin(player.getMin());
            updatedPlayer.setGoals(player.getGoals());
            updatedPlayer.setAssists(player.getAssists());
            updatedPlayer.setPk(player.getPk());
            updatedPlayer.setCrdy(player.getCrdy());
            updatedPlayer.setCrdr(player.getCrdr());
            updatedPlayer.setXg(player.getXg());
            updatedPlayer.setXag(player.getXag());
            updatedPlayer.setTeam(player.getTeam());

            return playerRepository.save(updatedPlayer);
        }

        return null;
    }

    @Transactional
    public void deletePlayer(Long id){
        playerRepository.deleteById(id);
    }
}
